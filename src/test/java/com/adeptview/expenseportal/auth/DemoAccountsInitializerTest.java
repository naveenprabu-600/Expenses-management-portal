package com.adeptview.expenseportal.auth;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

import java.util.List;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

@ExtendWith(MockitoExtension.class)
class DemoAccountsInitializerTest {

    private final PasswordEncoder encoder = new BCryptPasswordEncoder(4);

    @Mock
    private UserRepository users;

    @Test
    void unlocksSeededAccountsWithTheConfiguredPhrase() {
        User alice = locked("alice");
        when(users.findByPasswordHash(DemoAccountsInitializer.LOCKED)).thenReturn(List.of(alice));

        new DemoAccountsInitializer(users, encoder, "demo phrase for tests").run(null);

        assertThat(encoder.matches("demo phrase for tests", alice.getPasswordHash())).isTrue();
    }

    @Test
    void leavesAccountsLockedWhenNoPhraseIsConfigured() {
        User alice = locked("alice");
        when(users.findByPasswordHash(DemoAccountsInitializer.LOCKED)).thenReturn(List.of(alice));

        new DemoAccountsInitializer(users, encoder, "").run(null);

        assertThat(alice.getPasswordHash()).isEqualTo(DemoAccountsInitializer.LOCKED);
    }

    private static User locked(String username) {
        return new User(username, username, DemoAccountsInitializer.LOCKED, Role.EMPLOYEE, "ENG");
    }
}
