package com.repairshop.app.media;

public class InvalidRepairImageException extends RuntimeException {

    private final String messageCode;

    public InvalidRepairImageException(String messageCode) {
        super(messageCode);
        this.messageCode = messageCode;
    }

    public String getMessageCode() {
        return messageCode;
    }
}
