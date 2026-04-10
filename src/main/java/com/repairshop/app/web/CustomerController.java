package com.repairshop.app.web;

import com.repairshop.app.customer.CustomerService;
import com.repairshop.app.customer.DuplicateCustomerPhoneException;
import com.repairshop.app.security.AuthenticatedShopUser;
import com.repairshop.app.security.CurrentUser;
import com.repairshop.app.shop.Shop;
import com.repairshop.app.shop.ShopService;
import com.repairshop.app.web.form.CustomerForm;
import jakarta.validation.Valid;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;

@Controller
public class CustomerController {

    private final ShopService shopService;
    private final CustomerService customerService;

    public CustomerController(ShopService shopService, CustomerService customerService) {
        this.shopService = shopService;
        this.customerService = customerService;
    }

    @GetMapping("/{shopSlug}/customers")
    public String list(@PathVariable String shopSlug, Authentication authentication, Model model) {
        AuthenticatedShopUser principal = CurrentUser.require(authentication);
        model.addAttribute("shop", loadShop(shopSlug));
        model.addAttribute("customers", customerService.listForShop(principal.shopId()));
        return "customers/list";
    }

    @GetMapping("/{shopSlug}/customers/new")
    public String newCustomer(@PathVariable String shopSlug, Authentication authentication, Model model) {
        CurrentUser.require(authentication);
        model.addAttribute("shop", loadShop(shopSlug));
        if (!model.containsAttribute("customerForm")) {
            model.addAttribute("customerForm", new CustomerForm());
        }
        model.addAttribute("editing", false);
        return "customers/form";
    }

    @PostMapping("/{shopSlug}/customers/new")
    public String createCustomer(
            @PathVariable String shopSlug,
            Authentication authentication,
            @Valid @ModelAttribute("customerForm") CustomerForm form,
            BindingResult bindingResult,
            Model model
    ) {
        AuthenticatedShopUser principal = CurrentUser.require(authentication);
        Shop shop = loadShop(shopSlug);
        if (bindingResult.hasErrors()) {
            model.addAttribute("shop", shop);
            model.addAttribute("editing", false);
            return "customers/form";
        }

        try {
            customerService.create(principal.shopId(), form);
            return "redirect:/" + shopSlug + "/customers?created";
        } catch (DuplicateCustomerPhoneException ex) {
            bindingResult.rejectValue("primaryPhone", "customer.primaryPhone.duplicate");
            model.addAttribute("shop", shop);
            model.addAttribute("editing", false);
            return "customers/form";
        }
    }

    @GetMapping("/{shopSlug}/customers/{customerId}")
    public String detail(
            @PathVariable String shopSlug,
            @PathVariable Long customerId,
            Authentication authentication,
            Model model
    ) {
        AuthenticatedShopUser principal = CurrentUser.require(authentication);
        model.addAttribute("shop", loadShop(shopSlug));
        model.addAttribute("customer", customerService.getDetail(principal.shopId(), customerId));
        return "customers/detail";
    }

    @GetMapping("/{shopSlug}/customers/{customerId}/edit")
    public String editCustomer(
            @PathVariable String shopSlug,
            @PathVariable Long customerId,
            Authentication authentication,
            Model model
    ) {
        AuthenticatedShopUser principal = CurrentUser.require(authentication);
        model.addAttribute("shop", loadShop(shopSlug));
        if (!model.containsAttribute("customerForm")) {
            model.addAttribute("customerForm", customerService.getForm(principal.shopId(), customerId));
        }
        model.addAttribute("editing", true);
        model.addAttribute("customerId", customerId);
        return "customers/form";
    }

    @PostMapping("/{shopSlug}/customers/{customerId}/edit")
    public String updateCustomer(
            @PathVariable String shopSlug,
            @PathVariable Long customerId,
            Authentication authentication,
            @Valid @ModelAttribute("customerForm") CustomerForm form,
            BindingResult bindingResult,
            Model model
    ) {
        AuthenticatedShopUser principal = CurrentUser.require(authentication);
        Shop shop = loadShop(shopSlug);
        if (bindingResult.hasErrors()) {
            model.addAttribute("shop", shop);
            model.addAttribute("editing", true);
            model.addAttribute("customerId", customerId);
            return "customers/form";
        }

        try {
            customerService.update(principal.shopId(), customerId, form);
            return "redirect:/" + shopSlug + "/customers/" + customerId;
        } catch (DuplicateCustomerPhoneException ex) {
            bindingResult.rejectValue("primaryPhone", "customer.primaryPhone.duplicate");
            model.addAttribute("shop", shop);
            model.addAttribute("editing", true);
            model.addAttribute("customerId", customerId);
            return "customers/form";
        }
    }

    private Shop loadShop(String shopSlug) {
        return shopService.getBySlugOrThrow(shopSlug);
    }
}
