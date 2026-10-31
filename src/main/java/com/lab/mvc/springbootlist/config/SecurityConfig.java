package com.lab.mvc.springbootlist.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.factory.PasswordEncoderFactories;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.provisioning.InMemoryUserDetailsManager;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
public class SecurityConfig {

    /**
     * Protects every route with HTTP Basic and keeps CSRF protection enabled.
     * Thymeleaf adds CSRF tokens to POST forms rendered by the application.
     */
    @Bean
    SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        return http
                .authorizeHttpRequests(authorize -> authorize
                        .anyRequest().authenticated())
                .httpBasic(Customizer.withDefaults())
                .csrf(Customizer.withDefaults())
                .build();
    }

    /** Creates the single application administrator from externally supplied credentials. */
    @Bean
    UserDetailsService userDetailsService(
            @Value("${app.security.username:}") String username,
            @Value("${app.security.password:}") String password,
            PasswordEncoder passwordEncoder) {
        if (username.isBlank() || password.isBlank()) {
            throw new IllegalStateException(
                    "Configure APP_SECURITY_USERNAME and APP_SECURITY_PASSWORD before starting the application.");
        }

        return new InMemoryUserDetailsManager(
                User.withUsername(username)
                        .password(passwordEncoder.encode(password))
                        .roles("ADMIN")
                        .build()
        );
    }

    @Bean
    PasswordEncoder passwordEncoder() {
        return PasswordEncoderFactories.createDelegatingPasswordEncoder();
    }
}
