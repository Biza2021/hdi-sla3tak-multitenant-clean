package com.repairshop.app.repair;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.NOT_FOUND)
public class RepairItemNotFoundException extends RuntimeException {

    public RepairItemNotFoundException(Long repairId) {
        super("Repair item not found for id " + repairId);
    }
}
