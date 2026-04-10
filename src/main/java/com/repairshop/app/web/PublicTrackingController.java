package com.repairshop.app.web;

import com.repairshop.app.repair.RepairService;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@Controller
public class PublicTrackingController {

    private final RepairService repairService;

    public PublicTrackingController(RepairService repairService) {
        this.repairService = repairService;
    }

    @GetMapping("/track/{token}")
    public String trackRepair(@PathVariable String token, Model model, HttpServletResponse response) {
        return repairService.findPublicTracking(token)
                .map(tracking -> {
                    model.addAttribute("tracking", tracking);
                    return "tracking/detail";
                })
                .orElseGet(() -> {
                    response.setStatus(HttpStatus.NOT_FOUND.value());
                    return "tracking/not-found";
                });
    }
}
