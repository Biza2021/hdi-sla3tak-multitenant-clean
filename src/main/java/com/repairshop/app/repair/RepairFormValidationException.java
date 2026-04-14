package com.repairshop.app.repair;

public class RepairFormValidationException extends RuntimeException {

    private final String field;
    private final String messageCode;

    public RepairFormValidationException(String field, String messageCode) {
        super(messageCode);
        this.field = field;
        this.messageCode = messageCode;
    }

    public String getField() {
        return field;
    }

    public String getMessageCode() {
        return messageCode;
    }
}
