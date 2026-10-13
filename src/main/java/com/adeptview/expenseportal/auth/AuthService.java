package com.adeptview.expenseportal.auth;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AuthService {

    private static final Logger log = LoggerFactory.getLogger(AuthService.class);

    private final UserRepository users;
    private final PasswordEncoder passwordEncoder;
    private final TokenService tokens;

    public AuthService(UserRepository users, PasswordEncoder passwordEncoder, TokenService tokens) {
        this.users = users;
        this.passwordEncoder = passwordEncoder;
        this.tokens = tokens;
    }

    @Transactional(readOnly = true)
    public LoginResponse login(LoginRequest request) {
        User user = users.findByUsername(request.username()).orElseThrow();
        if (!passwordEncoder.matches(request.password(), user.getPasswordHash())) {
            log.info("Login failed: bad credentials for user={}", user.getUsername());
            throw new BadCredentialsException("Invalid username or password");
        }

        log.info("Login succeeded for user={}", user.getUsername());
        return new LoginResponse(tokens.issue(user), user.getUsername(), user.getDisplayName(), user.getRole());
    }

    @Transactional(readOnly = true)
    public ProfileView profile(CurrentUser current) {
        return users.findById(current.id())
                .map(user -> new ProfileView(user.getUsername(), user.getDisplayName(), user.getRole()))
                .orElseThrow(() -> new BadCredentialsException("The signed-in user no longer exists"));
    }
}
