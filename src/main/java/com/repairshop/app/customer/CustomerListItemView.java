package com.repairshop.app.customer;

public record CustomerListItemView(
        Long id,
        String fullName,
        String primaryPhone,
        String secondaryPhone,
        String notes
) {
}
