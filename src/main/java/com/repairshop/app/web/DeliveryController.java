package com.repairshop.app.web;

import com.repairshop.app.communication.RepairCommunicationContext;
import com.repairshop.app.communication.RepairCommunicationLinkService;
import com.repairshop.app.repair.DeliverySearchResultView;
import com.repairshop.app.repair.DeliverySearchSuggestionView;
import com.repairshop.app.repair.RepairNotReadyForDeliveryException;
import com.repairshop.app.repair.RepairItemStatus;
import com.repairshop.app.repair.RepairService;
import com.repairshop.app.security.AuthenticatedShopUser;
import com.repairshop.app.security.CurrentUser;
import com.repairshop.app.shop.Shop;
import com.repairshop.app.shop.ShopService;
import org.springframework.http.MediaType;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.util.UriComponentsBuilder;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.util.List;
import java.util.Locale;
import java.util.Optional;

@Controller
public class DeliveryController {

    private final ShopService shopService;
    private final RepairService repairService;
    private final RepairCommunicationLinkService repairCommunicationLinkService;

    public DeliveryController(
            ShopService shopService,
            RepairService repairService,
            RepairCommunicationLinkService repairCommunicationLinkService
    ) {
        this.shopService = shopService;
        this.repairService = repairService;
        this.repairCommunicationLinkService = repairCommunicationLinkService;
    }

    @GetMapping("/{shopSlug}/delivery")
    public String deliveryPage(
            @PathVariable String shopSlug,
            @RequestParam(name = "query", required = false) String query,
            @RequestParam(name = "pickupCode", required = false) String pickupCode,
            @RequestParam(name = "repairId", required = false) Long repairId,
            @RequestParam(name = "delivery", required = false) String deliveryFeedback,
            Authentication authentication,
            Locale locale,
            Model model
    ) {
        AuthenticatedShopUser principal = CurrentUser.require(authentication);
        String searchQuery = firstNonBlank(query, pickupCode);
        Shop shop = shopService.getBySlugOrThrow(shopSlug);
        Optional<DeliverySearchResultView> deliveryResult = repairService.findForDelivery(principal.shopId(), searchQuery, repairId);

        model.addAttribute("shop", shop);
        model.addAttribute("query", searchQuery);
        model.addAttribute("repairId", repairId);
        model.addAttribute("lookupAttempted", hasText(searchQuery) || repairId != null);
        model.addAttribute("deliveryFeedback", normalizeFeedback(deliveryFeedback));
        model.addAttribute("deliveryResult", deliveryResult.orElse(null));
        model.addAttribute(
                "pickupMessageLinks",
                deliveryResult
                        .filter(result -> result.status() == RepairItemStatus.READY_FOR_PICKUP)
                        .map(result -> repairCommunicationLinkService.buildReadyForPickupLinks(
                                buildCommunicationContext(shop, result),
                                locale,
                                ServletUriComponentsBuilder.fromCurrentContextPath().build().toUriString()
                        ))
                        .orElse(null)
        );
        return "delivery/index";
    }

    @GetMapping(value = "/{shopSlug}/delivery/suggestions", produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public List<DeliverySearchSuggestionView> deliverySuggestions(
            @PathVariable String shopSlug,
            @RequestParam(name = "q", required = false) String query,
            Authentication authentication
    ) {
        AuthenticatedShopUser principal = CurrentUser.require(authentication);
        shopService.getBySlugOrThrow(shopSlug);
        return repairService.suggestForDelivery(principal.shopId(), query);
    }

    @PostMapping("/{shopSlug}/delivery/{repairId}/deliver")
    public String markDelivered(
            @PathVariable String shopSlug,
            @PathVariable Long repairId,
            @RequestParam(name = "query", required = false) String query,
            @RequestParam(name = "pickupCode", required = false) String pickupCode,
            @RequestParam(name = "selectedRepairId", required = false) Long selectedRepairId,
            Authentication authentication
    ) {
        AuthenticatedShopUser principal = CurrentUser.require(authentication);
        shopService.getBySlugOrThrow(shopSlug);
        String searchQuery = firstNonBlank(query, pickupCode);

        String feedback = "delivered";
        try {
            boolean delivered = repairService.markDelivered(principal.shopId(), principal.userId(), repairId);
            if (!delivered) {
                feedback = "already-delivered";
            }
        } catch (RepairNotReadyForDeliveryException ex) {
            feedback = "not-ready";
        }

        UriComponentsBuilder redirect = UriComponentsBuilder.fromPath("/{shopSlug}/delivery")
                .queryParam("delivery", feedback);
        if (hasText(searchQuery)) {
            redirect.queryParam("query", searchQuery);
        }
        if (selectedRepairId != null) {
            redirect.queryParam("repairId", selectedRepairId);
        }
        return "redirect:" + redirect.buildAndExpand(shopSlug).encode().toUriString();
    }

    private String normalizeFeedback(String deliveryFeedback) {
        if ("delivered".equals(deliveryFeedback)
                || "already-delivered".equals(deliveryFeedback)
                || "not-ready".equals(deliveryFeedback)) {
            return deliveryFeedback;
        }
        return null;
    }

    private String firstNonBlank(String primaryValue, String fallbackValue) {
        if (hasText(primaryValue)) {
            return primaryValue.trim();
        }
        if (hasText(fallbackValue)) {
            return fallbackValue.trim();
        }
        return null;
    }

    private boolean hasText(String value) {
        return value != null && !value.trim().isEmpty();
    }

    private RepairCommunicationContext buildCommunicationContext(Shop shop, DeliverySearchResultView repair) {
        return new RepairCommunicationContext(
                shop.getBusinessName(),
                repair.customerName(),
                repair.customerPrimaryPhone(),
                repair.customerSecondaryPhone(),
                repair.title(),
                repair.pickupCode(),
                repair.publicTrackingToken()
        );
    }
}
