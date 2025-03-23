package com.cars24.fraud_detection.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
public class SecurityConfig {
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .csrf(csrf -> csrf.disable())  // Correct way to disable CSRF in Spring Security 6.1+
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/agents/**", "/documents/**", "/audio/**","/leads/**").permitAll() // Allow register API without authentication
                        .anyRequest().authenticated()
                );

        return http.build();
    }

}

