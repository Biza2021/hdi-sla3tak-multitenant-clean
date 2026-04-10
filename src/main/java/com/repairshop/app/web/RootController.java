package com.repairshop.app.web;

import com.repairshop.app.security.AuthenticatedShopUser;
import com.repairshop.app.shop.ShopService;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class RootController {

    private final ShopService shopService;

    public RootController(ShopService shopService) {
        this.shopService = shopService;
    }

    @GetMapping("/")
    public String root(Authentication authentication, HttpServletResponse response) {
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

        response.setStatus(HttpServletResponse.SC_NOT_FOUND);
        return "root/ambiguous";
    }
}

