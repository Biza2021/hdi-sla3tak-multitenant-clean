package com.repairshop.app.repair;

public class DuplicatePickupCodeException extends RuntimeException {

    public DuplicatePickupCodeException(String pickupCode) {
        super("Pickup code " + pickupCode + " already exists in this shop.");
    }
}
