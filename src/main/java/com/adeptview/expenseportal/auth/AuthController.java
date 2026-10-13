package com.adeptview.expenseportal.auth;

import org.springframework.http.HttpHeaders;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthService auth;

    public AuthController(AuthService auth) {
        this.auth = auth;
    }

    /**
     * Exchanges a username and password, sent as HTTP Basic credentials, for an
     * access token. A GET changes nothing on the server, so it needs no CSRF
     * exemption; every other call then carries the token as a bearer token.
     */
    @GetMapping("/token")
    public LoginResponse token(@RequestHeader(value = HttpHeaders.AUTHORIZATION, required = false) String authorization) {
        return auth.login(LoginRequest.fromBasicAuthorization(authorization));
    }

    @GetMapping("/me")
    public ProfileView me(@AuthenticationPrincipal Jwt jwt) {
        return auth.profile(CurrentUser.from(jwt));
    }
}
