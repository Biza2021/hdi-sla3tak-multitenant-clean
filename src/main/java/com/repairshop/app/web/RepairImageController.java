package com.repairshop.app.web;

import com.repairshop.app.media.RepairImageStorageService;
import com.repairshop.app.media.StoredRepairImageContent;
import com.repairshop.app.security.AuthenticatedShopUser;
import com.repairshop.app.security.CurrentUser;
import com.repairshop.app.shop.ShopService;
import org.springframework.http.CacheControl;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import java.nio.charset.StandardCharsets;

@Controller
public class RepairImageController {

    private final ShopService shopService;
    private final RepairImageStorageService repairImageStorageService;

    public RepairImageController(ShopService shopService, RepairImageStorageService repairImageStorageService) {
        this.shopService = shopService;
        this.repairImageStorageService = repairImageStorageService;
    }

    @GetMapping("/{shopSlug}/items/{repairId}/image")
    public ResponseEntity<org.springframework.core.io.Resource> serveRepairImage(
            @PathVariable String shopSlug,
            @PathVariable Long repairId,
            Authentication authentication
    ) {
        AuthenticatedShopUser principal = CurrentUser.require(authentication);
        shopService.getBySlugOrThrow(shopSlug);
        StoredRepairImageContent content = repairImageStorageService.loadForRepair(principal.shopId(), repairId);

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
