package com.adeptview.expenseportal.auth;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.Duration;

import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;

import com.nimbusds.jose.jwk.source.ImmutableSecret;

import org.junit.jupiter.api.Test;
import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;
import org.springframework.security.oauth2.jwt.NimbusJwtEncoder;
import org.springframework.test.util.ReflectionTestUtils;

class TokenServiceTest {

    @Test
    void issuesATokenThatCarriesTheUserAndTheirRole() throws Exception {
        SecretKey key = KeyGenerator.getInstance("HmacSHA256").generateKey();
        TokenService tokens = new TokenService(new NimbusJwtEncoder(new ImmutableSecret<>(key)), Duration.ofHours(1));

        User nina = new User("nina", "Nina Varga", "unused", Role.MANAGER, "FIN");
        ReflectionTestUtils.setField(nina, "id", 4L);

        Jwt jwt = NimbusJwtDecoder.withSecretKey(key).macAlgorithm(MacAlgorithm.HS256).build()
                .decode(tokens.issue(nina));
        CurrentUser current = CurrentUser.from(jwt);

        assertThat(jwt.getSubject()).isEqualTo("nina");
        assertThat(current.id()).isEqualTo(4L);
        assertThat(current.role()).isEqualTo(Role.MANAGER);
    }
}
