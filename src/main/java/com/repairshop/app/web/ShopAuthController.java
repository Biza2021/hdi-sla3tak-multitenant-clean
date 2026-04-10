package com.repairshop.app.web;

import com.repairshop.app.security.AuthenticatedShopUser;
import com.repairshop.app.security.CurrentUser;
import com.repairshop.app.security.ShopLoginAuthenticationToken;
import com.repairshop.app.shop.ShopService;
import com.repairshop.app.web.form.ShopLoginForm;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.context.HttpSessionSecurityContextRepository;
import org.springframework.security.web.authentication.logout.SecurityContextLogoutHandler;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;

@Controller
public class ShopAuthController {

    private final ShopService shopService;
    private final AuthenticationManager authenticationManager;
    private final SecurityContextLogoutHandler logoutHandler = new SecurityContextLogoutHandler();

    public ShopAuthController(ShopService shopService, AuthenticationManager authenticationManager) {
        this.shopService = shopService;
        this.authenticationManager = authenticationManager;
    }

    @GetMapping("/{shopSlug}/login")
    public String loginPage(
            @PathVariable String shopSlug,
            Authentication authentication,
            Model model
    ) {
        var shop = shopService.getBySlugOrThrow(shopSlug);

        if (authentication != null && authentication.getPrincipal() instanceof AuthenticatedShopUser principal) {
            return "redirect:/" + principal.shopSlug() + "/dashboard";
        }

        if (!model.containsAttribute("loginForm")) {
            model.addAttribute("loginForm", new ShopLoginForm());
        }
        model.addAttribute("shop", shop);
        return "auth/login";
    }

    @PostMapping("/{shopSlug}/login")
    public String login(
            @PathVariable String shopSlug,
            @Valid @ModelAttribute("loginForm") ShopLoginForm form,
            BindingResult bindingResult,
            HttpServletRequest request,
            HttpServletResponse response,
            Model model
    ) {
        var shop = shopService.getBySlugOrThrow(shopSlug);
        if (bindingResult.hasErrors()) {
            model.addAttribute("shop", shop);
            return "auth/login";
        }

        try {
            Authentication authenticated = authenticationManager.authenticate(
                    ShopLoginAuthenticationToken.unauthenticated(shopSlug, form.getUsername(), form.getPassword())
            );

            SecurityContext context = SecurityContextHolder.createEmptyContext();
            context.setAuthentication(authenticated);
            SecurityContextHolder.setContext(context);
            new HttpSessionSecurityContextRepository().saveContext(context, request, response);
            return "redirect:/" + shop.getSlug() + "/dashboard";
        } catch (AuthenticationException ex) {
            bindingResult.reject("auth.login.invalid", "Incorrect username or password.");
            model.addAttribute("shop", shop);
            return "auth/login";
        }
    }

    @PostMapping("/{shopSlug}/logout")
    public String logout(
            @PathVariable String shopSlug,
            Authentication authentication,
            HttpServletRequest request,
            HttpServletResponse response
    ) {
        CurrentUser.require(authentication);
        logoutHandler.logout(request, response, authentication);
        return "redirect:/" + shopSlug + "/login?loggedOut";
    }
}
