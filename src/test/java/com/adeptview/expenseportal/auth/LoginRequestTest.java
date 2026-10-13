package com.adeptview.expenseportal.auth;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.nio.charset.StandardCharsets;
import java.util.Base64;

import org.junit.jupiter.api.Test;
import org.springframework.security.authentication.BadCredentialsException;

class LoginRequestTest {

    @Test
    void readsTheUsernameAndPasswordFromBasicCredentials() {
        LoginRequest request = LoginRequest.fromBasicAuthorization(basic("alice:with:colons"));

        assertThat(request.username()).isEqualTo("alice");
        assertThat(request.password()).isEqualTo("with:colons");
    }

    @Test
    void refusesAMissingOrDifferentScheme() {
        assertThatThrownBy(() -> LoginRequest.fromBasicAuthorization(null)).isInstanceOf(BadCredentialsException.class);
        assertThatThrownBy(() -> LoginRequest.fromBasicAuthorization("Bearer abc"))
                .isInstanceOf(BadCredentialsException.class);
    }

    @Test
    void refusesCredentialsThatAreNotWellFormed() {
        assertThatThrownBy(() -> LoginRequest.fromBasicAuthorization("Basic not-base64!"))
                .isInstanceOf(BadCredentialsException.class);
        assertThatThrownBy(() -> LoginRequest.fromBasicAuthorization(basic("no-colon")))
                .isInstanceOf(BadCredentialsException.class);
        assertThatThrownBy(() -> LoginRequest.fromBasicAuthorization(basic(":missing-username")))
                .isInstanceOf(BadCredentialsException.class);
    }

    private static String basic(String credentials) {
        return "Basic " + Base64.getEncoder().encodeToString(credentials.getBytes(StandardCharsets.UTF_8));
    }
}
