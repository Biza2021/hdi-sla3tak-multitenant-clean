package com.repairshop.app.repair;

import java.util.Locale;

public enum DeliverySettlementChoice {
    FULLY_PAID,
    PARTIALLY_PAID,
    UNPAID;

    public static DeliverySettlementChoice from(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }

        try {
            return DeliverySettlementChoice.valueOf(value.trim().toUpperCase(Locale.ROOT));
        } catch (IllegalArgumentException ex) {
            return null;
        }
    }
}
