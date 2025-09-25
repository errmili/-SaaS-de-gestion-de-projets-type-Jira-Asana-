package com.projectsaas.analytics.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                .csrf(AbstractHttpConfigurer::disable)
                .authorizeHttpRequests(authz -> authz
                        .requestMatchers("/api/analytics/health").permitAll()
                        .requestMatchers("/api/analytics/**").permitAll() // Temporaire pour les tests
                        .requestMatchers("/api/reports/**").permitAll()
                        .requestMatchers("/api/metrics/**").permitAll()
                        .anyRequest().authenticated()
                );

        return http.build();
    }
}