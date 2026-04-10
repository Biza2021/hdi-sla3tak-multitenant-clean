package com.repairshop.app.web;

import com.repairshop.app.security.CurrentUser;
import com.repairshop.app.shop.ShopService;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@Controller
public class DashboardController {

    private final ShopService shopService;

    public DashboardController(ShopService shopService) {
        this.shopService = shopService;
    }

    @GetMapping("/{shopSlug}/dashboard")
    public String dashboard(@PathVariable String shopSlug, Authentication authentication, Model model) {
        CurrentUser.require(authentication);
        model.addAttribute("shop", shopService.getBySlugOrThrow(shopSlug));
        return "dashboard/index";
    }
}
