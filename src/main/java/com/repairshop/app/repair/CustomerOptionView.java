package com.repairshop.app.repair;

public record CustomerOptionView(
        Long id,
        String fullName,
        String primaryPhone,
        String secondaryPhone
) {
}
