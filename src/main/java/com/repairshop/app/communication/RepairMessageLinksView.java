package com.repairshop.app.communication;

public record RepairMessageLinksView(
        String targetPhone,
        String whatsappUrl,
        String smsUrl
) {

    public boolean available() {
        return whatsappUrl != null && smsUrl != null;
    }

    public boolean hasAnyLink() {
        return whatsappUrl != null || smsUrl != null;
    }

    public boolean hasWhatsApp() {
        return whatsappUrl != null;
    }

    public boolean hasSms() {
        return smsUrl != null;
    }
}
