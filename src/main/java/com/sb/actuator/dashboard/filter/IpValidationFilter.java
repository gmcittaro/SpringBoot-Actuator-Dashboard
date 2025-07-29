package com.sb.actuator.dashboard.filter;

import java.io.IOException;
import java.util.Collections;

import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import com.sb.actuator.dashboard.service.IpValidationService;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

/**
 * Filter for validating authorized IPs for Actuator endpoints.
 * Implements OncePerRequestFilter pattern to ensure single execution per request.
 */
@Component
public class IpValidationFilter extends OncePerRequestFilter {

    private static final String ACTUATOR_PATH = "/actuator";
    private static final String IP_AUTHENTICATED_USER = "IP_AUTHENTICATED";
    private static final String ROLE_ACTUATOR = "ROLE_ACTUATOR";

    private final IpValidationService ipValidationService;

    public IpValidationFilter(IpValidationService ipValidationService) {
        this.ipValidationService = ipValidationService;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, 
                                   HttpServletResponse response, 
                                   FilterChain filterChain) throws ServletException, IOException {
        
        if (!isActuatorRequest(request)) {
            filterChain.doFilter(request, response);
            return;
        }

        String clientIp = extractClientIp(request);
        
        if (!isValidIp(clientIp)) {
            handleUnauthorizedAccess(response, clientIp);
            return;
        }

        authenticateUser();
        filterChain.doFilter(request, response);
    }

    /**
     * Verifies if the request is directed to Actuator endpoints.
     */
    private boolean isActuatorRequest(HttpServletRequest request) {
        return request.getRequestURI().startsWith(ACTUATOR_PATH);
    }

    /**
     * Extracts client IP considering proxies and load balancers.
     */
    private String extractClientIp(HttpServletRequest request) {
        // Check X-Forwarded-For header for proxy/load balancer
        String xForwardedFor = request.getHeader("X-Forwarded-For");
        if (StringUtils.hasText(xForwardedFor)) {
            return extractFirstIp(xForwardedFor);
        }

        // Check X-Real-IP header
        String xRealIp = request.getHeader("X-Real-IP");
        if (StringUtils.hasText(xRealIp)) {
            return xRealIp.trim();
        }

        // Direct IP
        return request.getRemoteAddr();
    }

    /**
     * Extracts the first IP from the X-Forwarded-For list.
     */
    private String extractFirstIp(String xForwardedFor) {
        String[] ips = xForwardedFor.split(",");
        return ips[0].trim();
    }

    /**
     * Validates the client IP.
     */
    private boolean isValidIp(String clientIp) {
        return StringUtils.hasText(clientIp) && 
               ipValidationService.isIpAllowed(clientIp);
    }

    /**
     * Handles unauthorized access with security logging.
     */
    private void handleUnauthorizedAccess(HttpServletResponse response, String clientIp) throws IOException {
        // Security logging (without exposing internal details)
        logger.warn("Unauthorized access attempt to Actuator endpoints from IP: " + 
                   (clientIp != null ? clientIp : "unknown"));

        response.setStatus(HttpStatus.FORBIDDEN.value());
        response.setContentType("application/json");
        response.getWriter().write("{\"error\":\"Access denied\",\"message\":\"Insufficient privileges\"}");
    }

    /**
     * Authenticates the user in Spring Security context.
     */
    private void authenticateUser() {
        var authentication = new UsernamePasswordAuthenticationToken(
            IP_AUTHENTICATED_USER,
            null,
            Collections.singletonList(new SimpleGrantedAuthority(ROLE_ACTUATOR))
        );
        
        SecurityContextHolder.getContext().setAuthentication(authentication);
    }

    /**
     * Determines if the filter should be applied to the request.
     * Optimization: only checks Actuator paths.
     */
    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        return !isActuatorRequest(request);
    }
}