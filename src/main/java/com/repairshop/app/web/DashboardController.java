package com.repairshop.app.web;

import com.repairshop.app.dashboard.DashboardService;
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
    private final DashboardService dashboardService;

    public DashboardController(ShopService shopService, DashboardService dashboardService) {
        this.shopService = shopService;
        this.dashboardService = dashboardService;
    }

    @GetMapping("/{shopSlug}/dashboard")
    public String dashboard(@PathVariable String shopSlug, Authentication authentication, Model model) {
        var principal = CurrentUser.require(authentication);
        model.addAttribute("shop", shopService.getBySlugOrThrow(shopSlug));
        model.addAttribute("dashboard", dashboardService.getDashboard(principal.shopId()));
        return "dashboard/index";
    }
}
