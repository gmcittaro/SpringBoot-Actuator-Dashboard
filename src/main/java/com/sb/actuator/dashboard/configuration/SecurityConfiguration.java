package com.sb.actuator.dashboard.configuration;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.web.header.writers.ReferrerPolicyHeaderWriter;

import com.sb.actuator.dashboard.filter.IpFilterAuthenticationProvider;
import com.sb.actuator.dashboard.filter.IpValidationFilter;

/**
 * Spring Security configuration optimized for performance and security.
 * Implements IP filtering for Actuator endpoints while keeping other endpoints public.
 */
@Configuration
@EnableWebSecurity
public class SecurityConfiguration {

    private final IpValidationFilter ipValidationFilter;
    private final IpFilterAuthenticationProvider ipAuthProvider;

    public SecurityConfiguration(IpValidationFilter ipValidationFilter, 
                               IpFilterAuthenticationProvider ipAuthProvider) {
        this.ipValidationFilter = ipValidationFilter;
        this.ipAuthProvider = ipAuthProvider;
    }

    /**
     * Security configuration for Actuator endpoints.
     * High priority to intercept /actuator/** paths first.
     */
    @Bean
    @Order(1)
    public SecurityFilterChain actuatorSecurityFilterChain(HttpSecurity http) throws Exception {
        return http
                .securityMatcher("/actuator/**")
                .authorizeHttpRequests(auth -> 
                    auth.requestMatchers("/actuator/**").authenticated()
                )
                .addFilterBefore(ipValidationFilter, UsernamePasswordAuthenticationFilter.class)
                .sessionManagement(session -> 
                    session.sessionCreationPolicy(SessionCreationPolicy.STATELESS)
                )
                .csrf(AbstractHttpConfigurer::disable)
                .headers(headers -> configureSecurityHeaders(headers))
                .build();
    }

    /**
     * Security configuration for all other endpoints.
     * Maintains public access as requested.
     */
    @Bean
    @Order(2)
    public SecurityFilterChain publicSecurityFilterChain(HttpSecurity http) throws Exception {
        return http
                .authorizeHttpRequests(auth -> 
                    auth.anyRequest().permitAll()
                )
                .sessionManagement(session -> 
                    session.sessionCreationPolicy(SessionCreationPolicy.IF_REQUIRED)
                )
                .csrf(csrf -> csrf.ignoringRequestMatchers("/api/**"))
                .headers(headers -> configureSecurityHeaders(headers))
                .build();
    }

    /**
     * Security headers configuration following best practices.
     */
    private void configureSecurityHeaders(
            org.springframework.security.config.annotation.web.configurers.HeadersConfigurer<?> headers) {
        headers
                .frameOptions(frameOptions -> frameOptions.deny())
                .contentTypeOptions(contentTypeOptions -> {})
                .httpStrictTransportSecurity(hsts -> hsts
                    .maxAgeInSeconds(31536000)
                    .includeSubDomains(true)
                    .preload(true)
                )
                .referrerPolicy(referrerPolicy -> 
                    referrerPolicy.policy(ReferrerPolicyHeaderWriter.ReferrerPolicy.STRICT_ORIGIN_WHEN_CROSS_ORIGIN)
                )
                .addHeaderWriter((request, response) -> {
                    response.setHeader("X-XSS-Protection", "1; mode=block");
                    response.setHeader("X-Content-Type-Options", "nosniff");
                    response.setHeader("Cache-Control", "no-cache, no-store, must-revalidate");
                    response.setHeader("Pragma", "no-cache");
                    response.setHeader("Expires", "0");
                });
    }

    /**
     * Custom authentication provider configuration.
     */
    @Autowired
    public void configureGlobal(AuthenticationManagerBuilder auth) {
        auth.authenticationProvider(ipAuthProvider);
    }
}
