package com.sb.actuator.dashboard.filter;

import java.util.Collections;

import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.stereotype.Component;

/**
 * Custom authentication provider for IP-based authentication.
 * Implements Strategy pattern for authentication.
 */
@Component
public class IpFilterAuthenticationProvider implements AuthenticationProvider {

    private static final String IP_AUTHENTICATED_USER = "IP_AUTHENTICATED";
    private static final String ROLE_ACTUATOR = "ROLE_ACTUATOR";

    /**
     * Authenticates the user if authentication is based on valid IP.
     */
    @Override
    public Authentication authenticate(Authentication authentication) throws AuthenticationException {
        if (!isIpBasedAuthentication(authentication)) {
            return null;
        }

        return createAuthenticatedToken();
    }

    /**
     * Verifies if this provider supports the authentication type.
     */
    @Override
    public boolean supports(Class<?> authentication) {
        return UsernamePasswordAuthenticationToken.class.isAssignableFrom(authentication);
    }

    /**
     * Verifies if authentication is based on IP validation.
     */
    private boolean isIpBasedAuthentication(Authentication authentication) {
        return authentication instanceof UsernamePasswordAuthenticationToken &&
               IP_AUTHENTICATED_USER.equals(authentication.getPrincipal());
    }

    /**
     * Creates authentication token with Actuator privileges.
     */
    private Authentication createAuthenticatedToken() {
        return new UsernamePasswordAuthenticationToken(
            IP_AUTHENTICATED_USER,
            null,
            Collections.singletonList(new SimpleGrantedAuthority(ROLE_ACTUATOR))
        );
    }
}