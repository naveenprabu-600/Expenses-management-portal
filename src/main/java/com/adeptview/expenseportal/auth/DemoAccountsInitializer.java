package com.adeptview.expenseportal.auth;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

/**
 * The seed data creates its accounts locked, so no password or password hash
 * lives in the repository. At start-up, if {@code DEMO_USER_PASSWORD} is set,
 * every still-locked account gets that password. Accounts that already have
 * one are left alone.
 */
@Component
public class DemoAccountsInitializer implements ApplicationRunner {

    static final String LOCKED = "!";

    private static final Logger log = LoggerFactory.getLogger(DemoAccountsInitializer.class);

    private final UserRepository users;
    private final PasswordEncoder passwordEncoder;
    private final String demoSecret;

    public DemoAccountsInitializer(UserRepository users, PasswordEncoder passwordEncoder,
            @Value("${expense.demo.password:}") String demoSecret) {
        this.users = users;
        this.passwordEncoder = passwordEncoder;
        this.demoSecret = demoSecret;
    }

    @Override
    @Transactional
    public void run(ApplicationArguments args) {
        List<User> locked = users.findByPasswordHash(LOCKED);
        if (locked.isEmpty()) {
            return;
        }
        if (demoSecret == null || demoSecret.isBlank()) {
            log.warn("{} seeded account(s) stay locked: DEMO_USER_PASSWORD is not set", locked.size());
            return;
        }

        String hash = passwordEncoder.encode(demoSecret);
        locked.forEach(user -> user.changePasswordHash(hash));
        log.info("Demo password set on {} seeded account(s)", locked.size());
    }
}
