package com.repairshop.app.web;

import com.repairshop.app.media.StoredRepairImageContent;
import com.repairshop.app.repair.RepairService;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.core.io.Resource;
import org.springframework.http.CacheControl;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import java.nio.charset.StandardCharsets;

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

    @GetMapping("/track/{token}/image")
    public ResponseEntity<Resource> trackRepairImage(@PathVariable String token) {
        StoredRepairImageContent content = repairService.loadPublicTrackingImage(token)
                .orElseThrow(() -> new org.springframework.web.server.ResponseStatusException(HttpStatus.NOT_FOUND));

        return ResponseEntity.ok()
                .cacheControl(CacheControl.noStore())
                .contentLength(content.fileSize())
                .contentType(MediaType.parseMediaType(content.contentType()))
                .header(HttpHeaders.CONTENT_DISPOSITION, ContentDisposition.inline()
                        .filename(content.originalFilename(), StandardCharsets.UTF_8)
                        .build()
                        .toString())
                .body(content.resource());
    }
}
