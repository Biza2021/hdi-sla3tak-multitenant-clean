package com.repairshop.app.media;

import com.repairshop.app.repair.RepairItem;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.FileSystemResource;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

@Service
public class RepairImageStorageService {

    private static final long MAX_FILE_SIZE_BYTES = 5L * 1024L * 1024L;
    private static final Map<String, String> ALLOWED_CONTENT_TYPES = Map.of(
            MediaType.IMAGE_JPEG_VALUE, ".jpg",
            MediaType.IMAGE_PNG_VALUE, ".png",
            "image/webp", ".webp"
    );

    private final RepairImageRepository repairImageRepository;
    private final Path rootPath;

    public RepairImageStorageService(
            RepairImageRepository repairImageRepository,
            @Value("${app.storage.repair-images-root}") String repairImagesRoot
    ) {
        this.repairImageRepository = repairImageRepository;
        this.rootPath = Paths.get(repairImagesRoot).toAbsolutePath().normalize();
    }

    @Transactional(readOnly = true)
    public Optional<RepairImageSummaryView> findSummary(Long shopId, Long repairId) {
        return repairImageRepository.findByRepairItemIdAndShopId(repairId, shopId)
                .map(this::toSummary);
    }

    @Transactional(readOnly = true)
    public Map<Long, RepairImageSummaryView> findSummariesByRepairIds(Long shopId, List<Long> repairIds) {
        if (repairIds == null || repairIds.isEmpty()) {
            return Map.of();
        }

        Map<Long, RepairImageSummaryView> summaries = new LinkedHashMap<>();
        for (RepairImage image : repairImageRepository.findAllByRepairItemIdInAndShopId(repairIds, shopId)) {
            summaries.put(image.getRepairItem().getId(), toSummary(image));
        }
        return summaries;
    }

    @Transactional
    public void storeForRepair(RepairItem repairItem, MultipartFile imageFile) {
        if (imageFile == null || imageFile.isEmpty()) {
            return;
        }

        String extension = validateAndResolveExtension(imageFile);
        Long shopId = repairItem.getShop().getId();
        Long repairId = repairItem.getId();
        String newStorageKey = buildStorageKey(shopId, repairId, extension);
        Path targetPath = resolveStoragePath(newStorageKey);

        RepairImage image = repairImageRepository.findByRepairItemIdAndShopId(repairId, shopId)
                .orElseGet(() -> {
                    RepairImage created = new RepairImage();
                    created.setShop(repairItem.getShop());
                    created.setRepairItem(repairItem);
                    return created;
                });

        String previousStorageKey = image.getStorageKey();

        try {
            Files.createDirectories(targetPath.getParent());
            try (InputStream inputStream = imageFile.getInputStream()) {
                Files.copy(inputStream, targetPath, StandardCopyOption.REPLACE_EXISTING);
            }

            image.setStorageKey(newStorageKey);
            image.setOriginalFilename(resolveOriginalFilename(imageFile.getOriginalFilename(), extension));
            image.setContentType(imageFile.getContentType());
            image.setFileSize(imageFile.getSize());
            repairImageRepository.save(image);
        } catch (IOException ex) {
            deleteQuietly(newStorageKey);
            throw new IllegalStateException("Failed to store repair image.", ex);
        } catch (RuntimeException ex) {
            deleteQuietly(newStorageKey);
            throw ex;
        }

        if (previousStorageKey != null && !previousStorageKey.equals(newStorageKey)) {
            deleteQuietly(previousStorageKey);
        }
    }

    @Transactional(readOnly = true)
    public StoredRepairImageContent loadForRepair(Long shopId, Long repairId) {
        return findContentForRepair(shopId, repairId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));
    }

    @Transactional(readOnly = true)
    public Optional<StoredRepairImageContent> findContentForRepair(Long shopId, Long repairId) {
        return repairImageRepository.findByRepairItemIdAndShopId(repairId, shopId)
                .flatMap(this::toStoredContent);
    }

    private String validateAndResolveExtension(MultipartFile imageFile) {
        if (imageFile.getSize() > MAX_FILE_SIZE_BYTES) {
            throw new InvalidRepairImageException("repair.image.tooLarge");
        }

        String contentType = imageFile.getContentType();
        String extension = contentType != null ? ALLOWED_CONTENT_TYPES.get(contentType) : null;
        if (extension == null) {
            throw new InvalidRepairImageException("repair.image.invalidType");
        }

        return extension;
    }

    private RepairImageSummaryView toSummary(RepairImage image) {
        return new RepairImageSummaryView(image.getOriginalFilename());
    }

    private Optional<StoredRepairImageContent> toStoredContent(RepairImage image) {
        Path storagePath = resolveStoragePath(image.getStorageKey());
        if (!Files.isReadable(storagePath) || !Files.isRegularFile(storagePath)) {
            return Optional.empty();
        }

        return Optional.of(new StoredRepairImageContent(
                new FileSystemResource(storagePath),
                image.getOriginalFilename(),
                image.getContentType(),
                image.getFileSize()
        ));
    }

    private String buildStorageKey(Long shopId, Long repairId, String extension) {
        return "shops/" + shopId + "/repairs/" + repairId + "/" + UUID.randomUUID() + extension;
    }

    private Path resolveStoragePath(String storageKey) {
        Path resolved = rootPath.resolve(storageKey).normalize();
        if (!resolved.startsWith(rootPath)) {
            throw new IllegalStateException("Repair image path escaped the storage root.");
        }
        return resolved;
    }

    private String resolveOriginalFilename(String originalFilename, String extension) {
        String candidate = StringUtils.cleanPath(originalFilename == null ? "" : originalFilename);
        candidate = StringUtils.getFilename(candidate);
        if (!StringUtils.hasText(candidate) || candidate.contains("..")) {
            return "repair-image" + extension;
        }
        return candidate.length() > 255 ? candidate.substring(candidate.length() - 255) : candidate;
    }

    private void deleteQuietly(String storageKey) {
        if (!StringUtils.hasText(storageKey)) {
            return;
        }

        try {
            Files.deleteIfExists(resolveStoragePath(storageKey));
        } catch (IOException ignored) {
        }
    }
}
