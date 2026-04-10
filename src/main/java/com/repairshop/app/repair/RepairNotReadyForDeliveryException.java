package com.repairshop.app.repair;

public class RepairNotReadyForDeliveryException extends RuntimeException {

    private final RepairItemStatus currentStatus;

    public RepairNotReadyForDeliveryException(RepairItemStatus currentStatus) {
        super("Repair is not ready for delivery.");
        this.currentStatus = currentStatus;
    }

    public RepairItemStatus getCurrentStatus() {
        return currentStatus;
    }
}
