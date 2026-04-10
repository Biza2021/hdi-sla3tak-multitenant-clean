package com.repairshop.app.config;

import com.repairshop.app.security.ShopAuthenticationEntryPoint;
import com.repairshop.app.security.ShopAuthenticationProvider;
import com.repairshop.app.security.ShopRouteAccessFilter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.AnonymousAuthenticationFilter;

@Configuration
public class SecurityConfig {

    @Bean
    PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    AuthenticationManager authenticationManager(AuthenticationConfiguration configuration) throws Exception {
        return configuration.getAuthenticationManager();
    }

    @Bean
    SecurityFilterChain securityFilterChain(
            HttpSecurity http,
            ShopAuthenticationProvider authenticationProvider,
            ShopAuthenticationEntryPoint authenticationEntryPoint,
            ShopRouteAccessFilter shopRouteAccessFilter
    ) throws Exception {
        http
                .csrf(Customizer.withDefaults())
                .formLogin(AbstractHttpConfigurer::disable)
                .logout(AbstractHttpConfigurer::disable)
                .authenticationProvider(authenticationProvider)
                .exceptionHandling(exceptionHandling -> exceptionHandling
                        .authenticationEntryPoint(authenticationEntryPoint)
                )
                .authorizeHttpRequests(authorize -> authorize
                        .requestMatchers("/", "/shops/new", "/*/login", "/css/**", "/images/**", "/track/**", "/error")
                        .permitAll()
                        .anyRequest().authenticated()
                )
                .addFilterAfter(shopRouteAccessFilter, AnonymousAuthenticationFilter.class);

        return http.build();
    }
}
