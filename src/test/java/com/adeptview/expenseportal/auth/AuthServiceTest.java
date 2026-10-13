package com.adeptview.expenseportal.auth;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import java.util.Optional;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.util.ReflectionTestUtils;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    private static final String PHRASE = UUID.randomUUID().toString();

    private final PasswordEncoder encoder = new BCryptPasswordEncoder(4);

    @Mock
    private UserRepository users;

    @Mock
    private TokenService tokens;

    private AuthService auth;

    @BeforeEach
    void setUp() {
        auth = new AuthService(users, encoder, tokens);
    }

    @Test
    void signsInWithTheRightPassword() {
        User alice = user("alice");
        when(users.findByUsername("alice")).thenReturn(Optional.of(alice));
        when(tokens.issue(alice)).thenReturn("signed-token");

        LoginResponse response = auth.login(new LoginRequest("alice", PHRASE));

        assertThat(response.token()).isEqualTo("signed-token");
        assertThat(response.username()).isEqualTo("alice");
        assertThat(response.role()).isEqualTo(Role.EMPLOYEE);
    }

    @Test
    void refusesTheWrongPassword() {
        when(users.findByUsername("alice")).thenReturn(Optional.of(user("alice")));

        assertThatThrownBy(() -> auth.login(new LoginRequest("alice", PHRASE + "-wrong")))
                .isInstanceOf(BadCredentialsException.class);
        verifyNoInteractions(tokens);
    }

    private User user(String username) {
        User user = new User(username, "Test User", encoder.encode(PHRASE), Role.EMPLOYEE, "ENG");
        ReflectionTestUtils.setField(user, "id", 7L);
        return user;
    }
}
