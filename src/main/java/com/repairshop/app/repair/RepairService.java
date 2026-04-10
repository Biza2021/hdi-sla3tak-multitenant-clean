package com.repairshop.app.repair;

import com.repairshop.app.customer.Customer;
import com.repairshop.app.customer.CustomerNotFoundException;
import com.repairshop.app.customer.CustomerRepository;
import com.repairshop.app.media.RepairImageStorageService;
import com.repairshop.app.media.RepairImageSummaryView;
import com.repairshop.app.shop.ShopRepository;
import com.repairshop.app.user.ShopUser;
import com.repairshop.app.user.ShopUserRepository;
import com.repairshop.app.web.form.RepairItemForm;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.ThreadLocalRandom;

@Service
public class RepairService {

    private static final char[] PICKUP_CODE_ALPHABET = "23456789ABCDEFGHJKLMNPQRSTUVWXYZ".toCharArray();

    private final RepairItemRepository repairItemRepository;
    private final RepairStatusHistoryRepository repairStatusHistoryRepository;
    private final CustomerRepository customerRepository;
    private final RepairImageStorageService repairImageStorageService;
    private final ShopRepository shopRepository;
    private final ShopUserRepository shopUserRepository;

    public RepairService(
            RepairItemRepository repairItemRepository,
            RepairStatusHistoryRepository repairStatusHistoryRepository,
            CustomerRepository customerRepository,
            RepairImageStorageService repairImageStorageService,
            ShopRepository shopRepository,
            ShopUserRepository shopUserRepository
    ) {
        this.repairItemRepository = repairItemRepository;
        this.repairStatusHistoryRepository = repairStatusHistoryRepository;
        this.customerRepository = customerRepository;
        this.repairImageStorageService = repairImageStorageService;
        this.shopRepository = shopRepository;
        this.shopUserRepository = shopUserRepository;
    }

    @Transactional(readOnly = true)
    public List<RepairListItemView> listForShop(Long shopId) {
        return repairItemRepository.findAllByShopIdWithCustomerOrderByUpdatedAtDesc(shopId).stream()
                .map(item -> new RepairListItemView(
                        item.getId(),
                        item.getTitle(),
                        item.getCustomer().getFullName(),
                        item.getStatus(),
                        item.getExpectedDeliveryDate(),
                        item.getPickupCode(),
                        item.getRemainingBalance()
                ))
                .toList();
    }

    @Transactional(readOnly = true)
    public List<CustomerOptionView> listCustomerOptions(Long shopId) {
        return customerRepository.findAllByShopIdOrderByFullNameAsc(shopId).stream()
                .map(customer -> new CustomerOptionView(
                        customer.getId(),
                        customer.getFullName(),
                        customer.getPrimaryPhone()
                ))
                .toList();
    }

    @Transactional(readOnly = true)
    public RepairDetailView getDetail(Long shopId, Long repairId) {
        RepairItem item = getDetailedRepair(shopId, repairId);
        RepairImageSummaryView image = repairImageStorageService.findSummary(shopId, repairId).orElse(null);
        List<RepairHistoryEntryView> history = repairStatusHistoryRepository.findTimelineByRepairItemIdAndShopId(repairId, shopId)
                .stream()
                .map(entry -> new RepairHistoryEntryView(
                        entry.getCreatedAt(),
                        entry.getStatus(),
                        entry.getChangedByUser() != null ? entry.getChangedByUser().getFullName() : null,
                        entry.getNotes()
                ))
                .toList();

        return new RepairDetailView(
                item.getId(),
                item.getTitle(),
                item.getDescription(),
                item.getRepairNotes(),
                item.getStatus(),
                item.getExpectedDeliveryDate(),
                item.getEstimatedPrice(),
                item.getDepositPaid(),
                item.getRemainingBalance(),
                item.getPickupCode(),
                item.getPublicTrackingToken(),
                image,
                new CustomerOptionView(
                        item.getCustomer().getId(),
                        item.getCustomer().getFullName(),
                        item.getCustomer().getPrimaryPhone()
                ),
                history
        );
    }

    @Transactional(readOnly = true)
    public RepairItemForm getForm(Long shopId, Long repairId) {
        RepairItem item = getDetailedRepair(shopId, repairId);
        RepairItemForm form = new RepairItemForm();
        form.setCustomerId(item.getCustomer().getId());
        form.setTitle(item.getTitle());
        form.setDescription(item.getDescription());
        form.setRepairNotes(item.getRepairNotes());
        form.setStatus(item.getStatus());
        form.setExpectedDeliveryDate(item.getExpectedDeliveryDate());
        form.setEstimatedPrice(item.getEstimatedPrice());
        form.setDepositPaid(item.getDepositPaid());
        form.setRemainingBalance(item.getRemainingBalance());
        return form;
    }

    @Transactional(readOnly = true)
    public java.util.Optional<RepairImageSummaryView> findImageSummary(Long shopId, Long repairId) {
        return repairImageStorageService.findSummary(shopId, repairId);
    }

    @Transactional(readOnly = true)
    public java.util.Optional<PublicTrackingView> findPublicTracking(String token) {
        if (token == null || token.isBlank()) {
            return java.util.Optional.empty();
        }

        return repairItemRepository.findByPublicTrackingTokenWithShop(token)
                .map(item -> new PublicTrackingView(
                        item.getShop().getBusinessName(),
                        item.getTitle(),
                        item.getStatus(),
                        item.getExpectedDeliveryDate()
                ));
    }

    @Transactional
    public Long create(Long shopId, Long userId, RepairItemForm form) {
        RepairItem item = new RepairItem();
        item.setShop(shopRepository.getReferenceById(shopId));
        item.setPublicTrackingToken(generateTrackingToken());
        item.setPickupCode(generatePickupCode(shopId));
        applyForm(item, shopId, form);
        RepairItem saved = repairItemRepository.save(item);
        repairImageStorageService.storeForRepair(saved, form.getImageFile());
        createHistoryEntry(saved, resolveActor(shopId, userId), saved.getStatus());
        return saved.getId();
    }

    @Transactional
    public void update(Long shopId, Long userId, Long repairId, RepairItemForm form) {
        RepairItem item = repairItemRepository.findByIdAndShopId(repairId, shopId)
                .orElseThrow(() -> new RepairItemNotFoundException(repairId));

        RepairItemStatus previousStatus = item.getStatus();
        applyForm(item, shopId, form);
        repairItemRepository.save(item);
        repairImageStorageService.storeForRepair(item, form.getImageFile());

        if (previousStatus != item.getStatus()) {
            createHistoryEntry(item, resolveActor(shopId, userId), item.getStatus());
        }
    }

    private RepairItem getDetailedRepair(Long shopId, Long repairId) {
        return repairItemRepository.findDetailedByIdAndShopId(repairId, shopId)
                .orElseThrow(() -> new RepairItemNotFoundException(repairId));
    }

    private void applyForm(RepairItem item, Long shopId, RepairItemForm form) {
        Customer customer = customerRepository.findByIdAndShopId(form.getCustomerId(), shopId)
                .orElseThrow(() -> new CustomerNotFoundException(form.getCustomerId()));

        item.setCustomer(customer);
        item.setTitle(normalizeRequired(form.getTitle()));
        item.setDescription(normalizeNullable(form.getDescription()));
        item.setRepairNotes(normalizeNullable(form.getRepairNotes()));
        item.setStatus(form.getStatus());
        item.setExpectedDeliveryDate(form.getExpectedDeliveryDate());
        item.setEstimatedPrice(normalizeMoney(form.getEstimatedPrice()));
        item.setDepositPaid(normalizeMoney(form.getDepositPaid()));
        item.setRemainingBalance(normalizeMoney(form.getRemainingBalance()));
    }

    private void createHistoryEntry(RepairItem item, ShopUser actor, RepairItemStatus status) {
        RepairStatusHistory history = new RepairStatusHistory();
        history.setShop(item.getShop());
        history.setRepairItem(item);
        history.setChangedByUser(actor);
        history.setStatus(status);
        history.setNotes(null);
        repairStatusHistoryRepository.save(history);
    }

    private ShopUser resolveActor(Long shopId, Long userId) {
        return shopUserRepository.findByIdAndShopId(userId, shopId).orElse(null);
    }

    private String generateTrackingToken() {
        for (int attempt = 0; attempt < 12; attempt++) {
            String token = UUID.randomUUID().toString().replace("-", "");
            if (!repairItemRepository.existsByPublicTrackingToken(token)) {
                return token;
            }
        }
        throw new IllegalStateException("Unable to generate a unique tracking token.");
    }

    private String generatePickupCode(Long shopId) {
        ThreadLocalRandom random = ThreadLocalRandom.current();
        for (int attempt = 0; attempt < 24; attempt++) {
            StringBuilder code = new StringBuilder("PK-");
            for (int index = 0; index < 6; index++) {
                code.append(PICKUP_CODE_ALPHABET[random.nextInt(PICKUP_CODE_ALPHABET.length)]);
            }
            String candidate = code.toString();
            if (!repairItemRepository.existsByShopIdAndPickupCode(shopId, candidate)) {
                return candidate;
            }
        }
        throw new IllegalStateException("Unable to generate a unique pickup code.");
    }

    private BigDecimal normalizeMoney(BigDecimal value) {
        return (value == null ? BigDecimal.ZERO : value).setScale(2, RoundingMode.HALF_UP);
    }

    private String normalizeRequired(String value) {
        return value == null ? null : value.trim();
    }

    private String normalizeNullable(String value) {
        if (value == null) {
            return null;
        }
        String trimmed = value.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }
}
