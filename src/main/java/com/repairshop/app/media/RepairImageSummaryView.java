package com.repairshop.app.media;

public record RepairImageSummaryView(
        String originalFilename,
        String contentType,
        long fileSize,
        String fileSizeLabel
) {
}
