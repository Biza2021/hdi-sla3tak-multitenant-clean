package com.repairshop.app.customer;

import com.repairshop.app.repair.CustomerOptionView;

import java.util.Map;

public record QuickAddCustomerResponse(
        boolean success,
        CustomerOptionView customer,
        Map<String, String> errors
) {

    public static QuickAddCustomerResponse success(CustomerOptionView customer) {
        return new QuickAddCustomerResponse(true, customer, Map.of());
    }

    public static QuickAddCustomerResponse failure(Map<String, String> errors) {
        return new QuickAddCustomerResponse(false, null, errors);
    }
}
