package com.repairshop.app.web;

import com.repairshop.app.common.NormalizationUtils;
import com.repairshop.app.common.ShopRouting;
import com.repairshop.app.security.AuthenticatedShopUser;
import com.repairshop.app.shop.ShopService;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
public class RootController {

    private final ShopService shopService;

    public RootController(ShopService shopService) {
        this.shopService = shopService;
    }

    @GetMapping("/")
    public String root(
            @RequestParam(name = "shop", required = false) String shopSlug,
            Authentication authentication,
            Model model
    ) {
        if (authentication != null && authentication.getPrincipal() instanceof AuthenticatedShopUser principal) {
            return "redirect:/" + principal.shopSlug() + "/dashboard";
        }

        long shopCount = shopService.countShops();
        if (shopCount == 0) {
            return "redirect:/shops/new";
        }
        if (shopCount == 1) {
            return shopService.findOnlyShop()
                    .map(shop -> "redirect:/" + shop.getSlug() + "/login")
                    .orElse("redirect:/shops/new");
        }

        if (StringUtils.hasText(shopSlug)) {
            String normalizedSlug = NormalizationUtils.normalizeSlug(shopSlug);
            if (!ShopRouting.isReservedSlug(normalizedSlug) && shopService.slugExists(normalizedSlug)) {
                return "redirect:/" + normalizedSlug + "/login";
            }
            model.addAttribute("shopLookupError", true);
            model.addAttribute("enteredShopSlug", shopSlug.trim());
        }

        return "root/ambiguous";
    }
}
