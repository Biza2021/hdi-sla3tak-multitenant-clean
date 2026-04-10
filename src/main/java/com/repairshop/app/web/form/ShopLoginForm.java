package com.repairshop.app.web.form;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public class ShopLoginForm {

    @NotBlank(message = "{auth.username.notBlank}")
    @Size(max = 60, message = "{auth.username.size}")
    private String username;

    @NotBlank(message = "{auth.password.notBlank}")
    @Size(max = 72, message = "{auth.password.size}")
    private String password;

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }
}

