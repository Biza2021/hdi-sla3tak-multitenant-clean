package com.repairshop.app.web;

import com.repairshop.app.customer.CustomerNotFoundException;
import com.repairshop.app.media.InvalidRepairImageException;
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

@Controller
public class RepairItemController {

    private final ShopService shopService;
    private final RepairService repairService;

    public RepairItemController(ShopService shopService, RepairService repairService) {
        this.shopService = shopService;
        this.repairService = repairService;
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
            model.addAttribute("repairItemForm", new RepairItemForm());
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
            Model model
    ) {
        AuthenticatedShopUser principal = CurrentUser.require(authentication);
        Shop shop = loadShop(shopSlug);
        if (bindingResult.hasErrors()) {
            model.addAttribute("shop", shop);
            populateRepairFormModel(model, principal.shopId(), false, null);
            return "repairs/form";
        }

        try {
            Long repairId = repairService.create(principal.shopId(), principal.userId(), form);
            return "redirect:/" + shopSlug + "/items/" + repairId;
        } catch (CustomerNotFoundException ex) {
            bindingResult.rejectValue("customerId", "repair.customer.invalid");
        } catch (InvalidRepairImageException ex) {
            bindingResult.rejectValue("imageFile", ex.getMessageCode());
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
            Model model
    ) {
        AuthenticatedShopUser principal = CurrentUser.require(authentication);
        model.addAttribute("shop", loadShop(shopSlug));
        model.addAttribute("repair", repairService.getDetail(principal.shopId(), repairId));
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
            Model model
    ) {
        AuthenticatedShopUser principal = CurrentUser.require(authentication);
        Shop shop = loadShop(shopSlug);
        if (bindingResult.hasErrors()) {
            model.addAttribute("shop", shop);
            populateRepairFormModel(model, principal.shopId(), true, repairId);
            return "repairs/form";
        }

        try {
            repairService.update(principal.shopId(), principal.userId(), repairId, form);
            return "redirect:/" + shopSlug + "/items/" + repairId;
        } catch (CustomerNotFoundException ex) {
            bindingResult.rejectValue("customerId", "repair.customer.invalid");
        } catch (InvalidRepairImageException ex) {
            bindingResult.rejectValue("imageFile", ex.getMessageCode());
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
}
