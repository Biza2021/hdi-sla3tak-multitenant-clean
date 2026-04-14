package com.repairshop.app.web;

import com.repairshop.app.communication.RepairCommunicationContext;
import com.repairshop.app.communication.RepairCommunicationLinkService;
import com.repairshop.app.customer.CustomerNotFoundException;
import com.repairshop.app.media.InvalidRepairImageException;
import com.repairshop.app.repair.RepairDetailView;
import com.repairshop.app.repair.RepairFormValidationException;
import com.repairshop.app.repair.RepairItemStatus;
import com.repairshop.app.repair.RepairService;
import com.repairshop.app.security.AuthenticatedShopUser;
import com.repairshop.app.security.CurrentUser;
import com.repairshop.app.shop.Shop;
import com.repairshop.app.shop.ShopService;
import com.repairshop.app.web.form.RepairItemForm;
import jakarta.validation.Valid;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.util.Locale;

@Controller
public class RepairItemController {

    private final ShopService shopService;
    private final RepairService repairService;
    private final RepairCommunicationLinkService repairCommunicationLinkService;

    public RepairItemController(
            ShopService shopService,
            RepairService repairService,
            RepairCommunicationLinkService repairCommunicationLinkService
    ) {
        this.shopService = shopService;
        this.repairService = repairService;
        this.repairCommunicationLinkService = repairCommunicationLinkService;
    }

    @GetMapping("/{shopSlug}/items")
    public String list(@PathVariable String shopSlug, Authentication authentication, Model model) {
        AuthenticatedShopUser principal = CurrentUser.require(authentication);
        model.addAttribute("shop", loadShop(shopSlug));
        model.addAttribute("repairs", repairService.listForShop(principal.shopId()));
        return "repairs/list";
    }

    @GetMapping("/{shopSlug}/items/new")
    public String newRepair(@PathVariable String shopSlug, Authentication authentication, Model model) {
        AuthenticatedShopUser principal = CurrentUser.require(authentication);
        model.addAttribute("shop", loadShop(shopSlug));
        if (!model.containsAttribute("repairItemForm")) {
            RepairItemForm form = new RepairItemForm();
            syncComputedRemainingBalance(form);
            model.addAttribute("repairItemForm", form);
        } else {
            syncComputedRemainingBalance((RepairItemForm) model.asMap().get("repairItemForm"));
        }
        populateRepairFormModel(model, principal.shopId(), false, null);
        return "repairs/form";
    }

    @PostMapping("/{shopSlug}/items/new")
    public String createRepair(
            @PathVariable String shopSlug,
            Authentication authentication,
            @Valid @ModelAttribute("repairItemForm") RepairItemForm form,
            BindingResult bindingResult,
            Model model,
            RedirectAttributes redirectAttributes
    ) {
        AuthenticatedShopUser principal = CurrentUser.require(authentication);
        Shop shop = loadShop(shopSlug);
        syncComputedRemainingBalance(form);
        if (bindingResult.hasErrors()) {
            model.addAttribute("shop", shop);
            populateRepairFormModel(model, principal.shopId(), false, null);
            return "repairs/form";
        }

        try {
            Long repairId = repairService.create(principal.shopId(), principal.userId(), form);
            redirectAttributes.addFlashAttribute("communicationPrompt", "tracking");
            return "redirect:/" + shopSlug + "/items/" + repairId;
        } catch (CustomerNotFoundException ex) {
            bindingResult.rejectValue("customerId", "repair.customer.invalid");
        } catch (InvalidRepairImageException ex) {
            bindingResult.rejectValue("imageFile", ex.getMessageCode());
        } catch (RepairFormValidationException ex) {
            bindingResult.rejectValue(ex.getField(), ex.getMessageCode());
        }

        model.addAttribute("shop", shop);
        populateRepairFormModel(model, principal.shopId(), false, null);
        return "repairs/form";
    }

    @GetMapping("/{shopSlug}/items/{repairId}")
    public String detail(
            @PathVariable String shopSlug,
            @PathVariable Long repairId,
            Authentication authentication,
            Locale locale,
            Model model
    ) {
        AuthenticatedShopUser principal = CurrentUser.require(authentication);
        Shop shop = loadShop(shopSlug);
        RepairDetailView repair = repairService.getDetail(principal.shopId(), repairId);
        String applicationBaseUrl = ServletUriComponentsBuilder.fromCurrentContextPath()
                .build()
                .toUriString();

        model.addAttribute("shop", shop);
        model.addAttribute("repair", repair);
        model.addAttribute(
                "trackingLinks",
                repairCommunicationLinkService.buildTrackingLinks(
                        buildCommunicationContext(shop, repair),
                        locale,
                        applicationBaseUrl
                )
        );
        model.addAttribute(
                "pickupLinks",
                repair.status() == RepairItemStatus.READY_FOR_PICKUP
                        ? repairCommunicationLinkService.buildReadyForPickupLinks(
                        buildCommunicationContext(shop, repair),
                        locale,
                        applicationBaseUrl
                )
                        : null
        );
        return "repairs/detail";
    }

    @GetMapping("/{shopSlug}/items/{repairId}/edit")
    public String editRepair(
            @PathVariable String shopSlug,
            @PathVariable Long repairId,
            Authentication authentication,
            Model model
    ) {
        AuthenticatedShopUser principal = CurrentUser.require(authentication);
        model.addAttribute("shop", loadShop(shopSlug));
        if (!model.containsAttribute("repairItemForm")) {
            model.addAttribute("repairItemForm", repairService.getForm(principal.shopId(), repairId));
        } else {
            syncComputedRemainingBalance((RepairItemForm) model.asMap().get("repairItemForm"));
        }
        populateRepairFormModel(model, principal.shopId(), true, repairId);
        return "repairs/form";
    }

    @PostMapping("/{shopSlug}/items/{repairId}/edit")
    public String updateRepair(
            @PathVariable String shopSlug,
            @PathVariable Long repairId,
            Authentication authentication,
            @Valid @ModelAttribute("repairItemForm") RepairItemForm form,
            BindingResult bindingResult,
            Model model,
            RedirectAttributes redirectAttributes
    ) {
        AuthenticatedShopUser principal = CurrentUser.require(authentication);
        Shop shop = loadShop(shopSlug);
        syncComputedRemainingBalance(form);
        if (bindingResult.hasErrors()) {
            model.addAttribute("shop", shop);
            populateRepairFormModel(model, principal.shopId(), true, repairId);
            return "repairs/form";
        }

        try {
            boolean transitionedToReadyForPickup = repairService.update(principal.shopId(), principal.userId(), repairId, form);
            if (transitionedToReadyForPickup) {
                redirectAttributes.addFlashAttribute("communicationPrompt", "pickup");
            }
            return "redirect:/" + shopSlug + "/items/" + repairId;
        } catch (CustomerNotFoundException ex) {
            bindingResult.rejectValue("customerId", "repair.customer.invalid");
        } catch (InvalidRepairImageException ex) {
            bindingResult.rejectValue("imageFile", ex.getMessageCode());
        } catch (RepairFormValidationException ex) {
            bindingResult.rejectValue(ex.getField(), ex.getMessageCode());
        }

        model.addAttribute("shop", shop);
        populateRepairFormModel(model, principal.shopId(), true, repairId);
        return "repairs/form";
    }

    private void populateRepairFormModel(
            Model model,
            Long shopId,
            boolean editing,
            Long repairId
    ) {
        model.addAttribute("editing", editing);
        model.addAttribute("customerOptions", repairService.listCustomerOptions(shopId));
        model.addAttribute("repairStatuses", RepairItemStatus.values());
        model.addAttribute("repairId", repairId);
        model.addAttribute("existingImage", repairId != null
                ? repairService.findImageSummary(shopId, repairId).orElse(null)
                : null);
    }

    private Shop loadShop(String shopSlug) {
        return shopService.getBySlugOrThrow(shopSlug);
    }

    private void syncComputedRemainingBalance(RepairItemForm form) {
        form.setRemainingBalance(repairService.previewRemainingBalance(form.getEstimatedPrice(), form.getDepositPaid()));
    }

    private RepairCommunicationContext buildCommunicationContext(Shop shop, RepairDetailView repair) {
        return new RepairCommunicationContext(
                shop.getBusinessName(),
                repair.customer().fullName(),
                repair.customer().primaryPhone(),
                repair.customer().secondaryPhone(),
                repair.title(),
                repair.pickupCode(),
                repair.publicTrackingToken()
        );
    }
}
