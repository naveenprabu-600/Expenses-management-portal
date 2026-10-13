package com.adeptview.expenseportal.config;

import java.nio.charset.StandardCharsets;

import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;

import com.adeptview.expenseportal.common.UserMdcFilter;
import com.nimbusds.jose.jwk.source.ImmutableSecret;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;
import org.springframework.security.oauth2.jwt.NimbusJwtEncoder;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationConverter;
import org.springframework.security.oauth2.server.resource.authentication.JwtGrantedAuthoritiesConverter;
import org.springframework.security.oauth2.server.resource.web.authentication.BearerTokenAuthenticationFilter;
import org.springframework.security.web.SecurityFilterChain;

/**
 * Stateless API security: a signed bearer token on every request except sign-in
 * and the health check, and a role per area.
 *
 * Requests carrying a bearer token are exempt from CSRF checks by the resource
 * server itself; sign-in is the one unauthenticated write and is exempted here.
 */
@Configuration
@EnableWebSecurity
public class SecurityConfig {

    private static final int MIN_KEY_LENGTH = 32;

    @Bean
    SecretKey jwtSigningKey(@Value("${expense.jwt.secret}") String secret) {
        if (secret == null || secret.length() < MIN_KEY_LENGTH) {
            throw new IllegalStateException(
                    "EXPENSE_JWT_SECRET must be set to at least " + MIN_KEY_LENGTH + " characters");
        }
        return new SecretKeySpec(secret.getBytes(StandardCharsets.UTF_8), "HmacSHA256");
    }

    @Bean
    JwtEncoder jwtEncoder(SecretKey jwtSigningKey) {
        return new NimbusJwtEncoder(new ImmutableSecret<>(jwtSigningKey));
    }

    @Bean
    JwtDecoder jwtDecoder(SecretKey jwtSigningKey) {
        return NimbusJwtDecoder.withSecretKey(jwtSigningKey).macAlgorithm(MacAlgorithm.HS256).build();
    }

    @Bean
    PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .csrf(csrf -> csrf.ignoringRequestMatchers("/api/auth/login"))
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers(HttpMethod.POST, "/api/auth/login").permitAll()
                        .requestMatchers("/actuator/health/**").permitAll()
                        .requestMatchers("/api/search/**", "/api/imports/**").hasRole("FINANCE")
                        .requestMatchers("/api/approvals/**").hasRole("MANAGER")
                        .anyRequest().authenticated())
                .oauth2ResourceServer(oauth -> oauth.jwt(jwt -> jwt.jwtAuthenticationConverter(rolesFromToken())))
                .addFilterAfter(new UserMdcFilter(), BearerTokenAuthenticationFilter.class);
        return http.build();
    }

    private static JwtAuthenticationConverter rolesFromToken() {
        JwtGrantedAuthoritiesConverter authorities = new JwtGrantedAuthoritiesConverter();
        authorities.setAuthoritiesClaimName("roles");
        authorities.setAuthorityPrefix("ROLE_");

        JwtAuthenticationConverter converter = new JwtAuthenticationConverter();
        converter.setJwtGrantedAuthoritiesConverter(authorities);
        return converter;
    }
}
