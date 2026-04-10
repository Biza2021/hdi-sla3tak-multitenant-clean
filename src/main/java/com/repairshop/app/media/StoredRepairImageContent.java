package com.repairshop.app.media;

import org.springframework.core.io.Resource;

public record StoredRepairImageContent(
        Resource resource,
        String originalFilename,
        String contentType,
        long fileSize
) {
}
