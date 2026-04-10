package com.repairshop.app.web.form;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public class CustomerForm {

    @NotBlank(message = "{customer.fullName.notBlank}")
    @Size(max = 120, message = "{customer.fullName.size}")
    private String fullName;

    @Size(max = 40, message = "{customer.primaryPhone.size}")
    private String primaryPhone;

    @Size(max = 40, message = "{customer.secondaryPhone.size}")
    private String secondaryPhone;

    @Size(max = 4000, message = "{customer.notes.size}")
    private String notes;

    public String getFullName() {
        return fullName;
    }

    public void setFullName(String fullName) {
        this.fullName = fullName;
    }

    public String getPrimaryPhone() {
        return primaryPhone;
    }

    public void setPrimaryPhone(String primaryPhone) {
        this.primaryPhone = primaryPhone;
    }

    public String getSecondaryPhone() {
        return secondaryPhone;
    }

    public void setSecondaryPhone(String secondaryPhone) {
        this.secondaryPhone = secondaryPhone;
    }

    public String getNotes() {
        return notes;
    }

    public void setNotes(String notes) {
        this.notes = notes;
    }
}
