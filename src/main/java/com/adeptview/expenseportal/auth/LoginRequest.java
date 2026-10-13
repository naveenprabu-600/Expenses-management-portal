package com.adeptview.expenseportal.auth;

import java.nio.charset.StandardCharsets;
import java.util.Base64;

import org.springframework.security.authentication.BadCredentialsException;

/**
 * A username and password, as sent in an HTTP Basic {@code Authorization}
 * header to obtain an access token.
 */
public record LoginRequest(String username, String password) {

    private static final String SCHEME = "Basic ";

    public static LoginRequest fromBasicAuthorization(String header) {
        if (header == null || !header.regionMatches(true, 0, SCHEME, 0, SCHEME.length())) {
            throw new BadCredentialsException("Sign in with a username and password");
        }

        String decoded;
        try {
            decoded = new String(Base64.getDecoder().decode(header.substring(SCHEME.length()).trim()),
                    StandardCharsets.UTF_8);
        } catch (IllegalArgumentException e) {
            throw new BadCredentialsException("Sign in with a username and password");
        }

        int colon = decoded.indexOf(':');
        if (colon <= 0) {
            throw new BadCredentialsException("Sign in with a username and password");
        }
        return new LoginRequest(decoded.substring(0, colon), decoded.substring(colon + 1));
    }
}
