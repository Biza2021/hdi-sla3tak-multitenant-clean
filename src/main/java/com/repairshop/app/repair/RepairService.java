package com.repairshop.app.repair;

import com.repairshop.app.customer.Customer;
import com.repairshop.app.customer.CustomerNotFoundException;
import com.repairshop.app.customer.CustomerRepository;
import com.repairshop.app.media.RepairImageStorageService;
import com.repairshop.app.media.RepairImageSummaryView;
import com.repairshop.app.media.StoredRepairImageContent;
import com.repairshop.app.shop.ShopRepository;
import com.repairshop.app.user.ShopUser;
import com.repairshop.app.user.ShopUserRepository;
import com.repairshop.app.web.form.RepairItemForm;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ThreadLocalRandom;

@Service
public class RepairService {

    private static final char[] PICKUP_CODE_ALPHABET = "23456789ABCDEFGHJKLMNPQRSTUVWXYZ".toCharArray();
    private static final int DELIVERY_SUGGESTION_LIMIT = 8;

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
        List<RepairItem> repairs = repairItemRepository.findAllByShopIdWithCustomerOrderByUpdatedAtDesc(shopId);
        java.util.Map<Long, RepairImageSummaryView> imageByRepairId = repairImageStorageService.findSummariesByRepairIds(
                shopId,
                repairs.stream().map(RepairItem::getId).toList()
        );

        return repairs.stream()
                .map(item -> new RepairListItemView(
                        item.getId(),
                        item.getTitle(),
                        item.getCustomer().getFullName(),
                        item.getStatus(),
                        item.getExpectedDeliveryDate(),
                        item.getPickupCode(),
                        item.getRemainingBalance(),
                        imageByRepairId.get(item.getId())
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
                        item.getCustomer().getPrimaryPhone(),
                        item.getCustomer().getSecondaryPhone()
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
        form.setRemainingBalance(calculateRemainingBalance(item.getEstimatedPrice(), item.getDepositPaid()));
        return form;
    }

    @Transactional(readOnly = true)
    public java.util.Optional<RepairImageSummaryView> findImageSummary(Long shopId, Long repairId) {
        return repairImageStorageService.findSummary(shopId, repairId);
    }

    @Transactional(readOnly = true)
    public java.util.Optional<DeliverySearchResultView> findForDeliveryByPickupCode(Long shopId, String pickupCode) {
        String normalizedPickupCode = normalizePickupCode(pickupCode);
        if (normalizedPickupCode == null) {
            return java.util.Optional.empty();
        }

        return repairItemRepository.findByPickupCodeAndShopIdWithCustomer(normalizedPickupCode, shopId)
                .map(item -> toDeliveryResult(shopId, item));
    }

    @Transactional(readOnly = true)
    public Optional<DeliverySearchResultView> findForDelivery(Long shopId, String query, Long repairId) {
        if (repairId != null) {
            return repairItemRepository.findDetailedByIdAndShopId(repairId, shopId)
                    .map(item -> toDeliveryResult(shopId, item));
        }

        DeliverySearchTerm searchTerm = DeliverySearchTerm.from(query);
        if (searchTerm == null) {
            return Optional.empty();
        }

        Optional<DeliverySearchResultView> exactPickupMatch = findForDeliveryByPickupCode(shopId, searchTerm.rawQuery());
        if (exactPickupMatch.isPresent()) {
            return exactPickupMatch;
        }

        return repairItemRepository.searchDeliveryMatches(
                        shopId,
                        searchTerm.lowerQuery(),
                        searchTerm.upperQuery(),
                        searchTerm.digitsQuery(),
                        searchTerm.phoneSearch(),
                        PageRequest.of(0, 1)
                ).stream()
                .findFirst()
                .map(item -> toDeliveryResult(shopId, item));
    }

    @Transactional(readOnly = true)
    public List<DeliverySearchSuggestionView> suggestForDelivery(Long shopId, String query) {
        DeliverySearchTerm searchTerm = DeliverySearchTerm.from(query);
        if (searchTerm == null || searchTerm.rawQuery().length() < 2) {
            return List.of();
        }

        return repairItemRepository.searchDeliveryMatches(
                        shopId,
                        searchTerm.lowerQuery(),
                        searchTerm.upperQuery(),
                        searchTerm.digitsQuery(),
                        searchTerm.phoneSearch(),
                        PageRequest.of(0, DELIVERY_SUGGESTION_LIMIT)
                ).stream()
                .map(item -> new DeliverySearchSuggestionView(
                        item.getId(),
                        item.getTitle(),
                        item.getCustomer().getFullName(),
                        item.getCustomer().getPrimaryPhone(),
                        item.getCustomer().getSecondaryPhone(),
                        item.getPickupCode(),
                        item.getStatus()
                ))
                .toList();
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
                        item.getExpectedDeliveryDate(),
                        item.getPickupCode(),
                        item.getPublicTrackingToken(),
                        repairImageStorageService.findContentForRepair(item.getShop().getId(), item.getId()).isPresent()
                ));
    }

    @Transactional(readOnly = true)
    public Optional<StoredRepairImageContent> loadPublicTrackingImage(String token) {
        if (token == null || token.isBlank()) {
            return Optional.empty();
        }

        return repairItemRepository.findByPublicTrackingToken(token)
                .flatMap(item -> repairImageStorageService.findContentForRepair(item.getShop().getId(), item.getId()));
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
    public boolean update(Long shopId, Long userId, Long repairId, RepairItemForm form) {
        RepairItem item = repairItemRepository.findByIdAndShopId(repairId, shopId)
                .orElseThrow(() -> new RepairItemNotFoundException(repairId));

        RepairItemStatus previousStatus = item.getStatus();
        applyForm(item, shopId, form);
        repairItemRepository.save(item);
        repairImageStorageService.storeForRepair(item, form.getImageFile());

        if (previousStatus != item.getStatus()) {
            createHistoryEntry(item, resolveActor(shopId, userId), item.getStatus());
        }

        return previousStatus != RepairItemStatus.READY_FOR_PICKUP
                && item.getStatus() == RepairItemStatus.READY_FOR_PICKUP;
    }

    @Transactional
    public DeliveryCompletionOutcome completeDelivery(
            Long shopId,
            Long userId,
            Long repairId,
            DeliverySettlementChoice settlementChoice,
            BigDecimal paymentReceivedNow
    ) {
        RepairItem item = repairItemRepository.findByIdAndShopId(repairId, shopId)
                .orElseThrow(() -> new RepairItemNotFoundException(repairId));

        if (item.getStatus().isDelivered()) {
            return DeliveryCompletionOutcome.ALREADY_DELIVERED;
        }
        if (!item.getStatus().canBeMarkedDelivered()) {
            throw new RepairNotReadyForDeliveryException(item.getStatus());
        }

        BigDecimal remainingBalance = normalizeMoney(item.getRemainingBalance());
        if (remainingBalance.signum() <= 0) {
            markAsDelivered(item, resolveActor(shopId, userId));
            return DeliveryCompletionOutcome.DELIVERED;
        }

        DeliverySettlementChoice normalizedChoice = settlementChoice == null
                ? DeliverySettlementChoice.UNPAID
                : settlementChoice;

        if (normalizedChoice == DeliverySettlementChoice.UNPAID) {
            return DeliveryCompletionOutcome.PAYMENT_REQUIRED;
        }

        if (normalizedChoice == DeliverySettlementChoice.FULLY_PAID) {
            applyCollectedAmount(item, remainingBalance);
            markAsDelivered(item, resolveActor(shopId, userId));
            return DeliveryCompletionOutcome.DELIVERED;
        }

        BigDecimal collectedAmount = normalizeMoney(paymentReceivedNow);
        if (collectedAmount.signum() <= 0 || collectedAmount.compareTo(remainingBalance) > 0) {
            return DeliveryCompletionOutcome.INVALID_SETTLEMENT;
        }
        if (collectedAmount.compareTo(remainingBalance) == 0) {
            applyCollectedAmount(item, collectedAmount);
            markAsDelivered(item, resolveActor(shopId, userId));
            return DeliveryCompletionOutcome.DELIVERED;
        }

        applyCollectedAmount(item, collectedAmount);
        repairItemRepository.save(item);
        return DeliveryCompletionOutcome.PARTIAL_PAYMENT_RECORDED;
    }

    public BigDecimal previewRemainingBalance(BigDecimal estimatedPrice, BigDecimal depositPaid) {
        BigDecimal normalizedEstimatedPrice = normalizeMoney(estimatedPrice);
        BigDecimal normalizedDepositPaid = normalizeMoney(depositPaid);
        if (normalizedDepositPaid.compareTo(normalizedEstimatedPrice) >= 0) {
            return BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP);
        }
        return calculateRemainingBalance(normalizedEstimatedPrice, normalizedDepositPaid);
    }

    private RepairItem getDetailedRepair(Long shopId, Long repairId) {
        return repairItemRepository.findDetailedByIdAndShopId(repairId, shopId)
                .orElseThrow(() -> new RepairItemNotFoundException(repairId));
    }

    private DeliverySearchResultView toDeliveryResult(Long shopId, RepairItem item) {
        return new DeliverySearchResultView(
                item.getId(),
                item.getTitle(),
                item.getCustomer().getFullName(),
                item.getCustomer().getPrimaryPhone(),
                item.getCustomer().getSecondaryPhone(),
                item.getStatus(),
                item.getExpectedDeliveryDate(),
                item.getPickupCode(),
                item.getPublicTrackingToken(),
                item.getEstimatedPrice(),
                item.getDepositPaid(),
                item.getRemainingBalance(),
                repairImageStorageService.findSummary(shopId, item.getId()).orElse(null)
        );
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
        BigDecimal estimatedPrice = normalizeMoney(form.getEstimatedPrice());
        BigDecimal depositPaid = normalizeMoney(form.getDepositPaid());
        if (depositPaid.compareTo(estimatedPrice) > 0) {
            throw new RepairFormValidationException("depositPaid", "repair.deposit.exceedsEstimated");
        }

        BigDecimal remainingBalance = calculateRemainingBalance(estimatedPrice, depositPaid);
        if (form.getStatus() == RepairItemStatus.DELIVERED && remainingBalance.signum() > 0) {
            throw new RepairFormValidationException("status", "repair.status.deliveryRequiresSettlement");
        }

        item.setEstimatedPrice(estimatedPrice);
        item.setDepositPaid(depositPaid);
        item.setRemainingBalance(remainingBalance);
    }

    private void applyCollectedAmount(RepairItem item, BigDecimal collectedAmount) {
        BigDecimal updatedDepositPaid = normalizeMoney(item.getDepositPaid()).add(normalizeMoney(collectedAmount));
        item.setDepositPaid(updatedDepositPaid);
        item.setRemainingBalance(calculateRemainingBalance(item.getEstimatedPrice(), updatedDepositPaid));
    }

    private void markAsDelivered(RepairItem item, ShopUser actor) {
        item.setStatus(RepairItemStatus.DELIVERED);
        repairItemRepository.save(item);
        createHistoryEntry(item, actor, RepairItemStatus.DELIVERED);
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

    private BigDecimal calculateRemainingBalance(BigDecimal estimatedPrice, BigDecimal depositPaid) {
        return normalizeMoney(estimatedPrice).subtract(normalizeMoney(depositPaid)).setScale(2, RoundingMode.HALF_UP);
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

    private String normalizePickupCode(String pickupCode) {
        if (pickupCode == null) {
            return null;
        }
        String normalized = pickupCode.trim().toUpperCase(Locale.ROOT);
        return normalized.isEmpty() ? null : normalized;
    }

    private record DeliverySearchTerm(
            String rawQuery,
            String lowerQuery,
            String upperQuery,
            String digitsQuery,
            boolean phoneSearch
    ) {

        private static DeliverySearchTerm from(String query) {
            if (query == null) {
                return null;
            }

            String trimmed = query.trim();
            if (trimmed.isEmpty()) {
                return null;
            }

            String digits = trimmed.replaceAll("\\D+", "");
            return new DeliverySearchTerm(
                    trimmed,
                    trimmed.toLowerCase(Locale.ROOT),
                    trimmed.toUpperCase(Locale.ROOT),
                    digits,
                    !digits.isEmpty()
            );
        }
    }
}
