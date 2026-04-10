package com.repairshop.app.web;

import com.repairshop.app.shop.ShopRegistrationRequest;
import com.repairshop.app.shop.ShopService;
import com.repairshop.app.web.form.ShopRegistrationForm;
import jakarta.validation.Valid;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/shops")
public class ShopOnboardingController {

    private final ShopService shopService;

    public ShopOnboardingController(ShopService shopService) {
        this.shopService = shopService;
    }

    @GetMapping("/new")
    public String newShop(Model model) {
        if (!model.containsAttribute("shopRegistrationForm")) {
            model.addAttribute("shopRegistrationForm", new ShopRegistrationForm());
        }
        return "shops/new";
    }

    @PostMapping("/new")
    public String createShop(
            @Valid @ModelAttribute("shopRegistrationForm") ShopRegistrationForm form,
            BindingResult bindingResult
    ) {
        if (shopService.isReservedSlug(form.getSlug())) {
            bindingResult.rejectValue("slug", "shop.slug.reserved");
        }
        if (!bindingResult.hasFieldErrors("slug") && shopService.slugExists(form.getSlug())) {
            bindingResult.rejectValue("slug", "shop.slug.duplicate");
        }
        if (bindingResult.hasErrors()) {
            return "shops/new";
        }

        try {
            var shop = shopService.registerShop(new ShopRegistrationRequest(
                    form.getBusinessName(),
                    form.getSlug(),
                    form.getOwnerFullName(),
                    form.getOwnerUsername(),
                    form.getOwnerPassword()
            ));

            return "redirect:/" + shop.getSlug() + "/login?created";
        } catch (IllegalArgumentException ex) {
            bindingResult.rejectValue("slug", "shop.slug.duplicate");
            return "shops/new";
        }
    }
}
