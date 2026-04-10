package com.repairshop.app.customer;

public class DuplicateCustomerPhoneException extends RuntimeException {

    public DuplicateCustomerPhoneException(String primaryPhone) {
        super("A customer with phone " + primaryPhone + " already exists in this shop.");
    }
}
