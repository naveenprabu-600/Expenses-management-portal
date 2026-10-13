package com.adeptview.expenseportal.auth;

import java.time.Duration;
import java.time.Instant;
import java.util.List;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.oauth2.jwt.JwsHeader;
import org.springframework.security.oauth2.jwt.JwtClaimsSet;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.JwtEncoderParameters;
import org.springframework.stereotype.Service;

/**
 * Issues the signed access tokens the web app sends with every request.
 */
@Service
public class TokenService {

    static final String ISSUER = "expense-portal-api";
    static final String USER_ID_CLAIM = "uid";
    static final String NAME_CLAIM = "name";
    static final String ROLES_CLAIM = "roles";

    private final JwtEncoder encoder;
    private final Duration ttl;

    public TokenService(JwtEncoder encoder, @Value("${expense.jwt.ttl}") Duration ttl) {
        this.encoder = encoder;
        this.ttl = ttl;
    }

    public String issue(User user) {
        Instant now = Instant.now();
        JwtClaimsSet claims = JwtClaimsSet.builder()
                .issuer(ISSUER)
                .issuedAt(now)
                .expiresAt(now.plus(ttl))
                .subject(user.getUsername())
                .claim(USER_ID_CLAIM, user.getId())
                .claim(NAME_CLAIM, user.getDisplayName())
                .claim(ROLES_CLAIM, List.of(user.getRole().name()))
                .build();
        JwsHeader header = JwsHeader.with(MacAlgorithm.HS256).build();
        return encoder.encode(JwtEncoderParameters.from(header, claims)).getTokenValue();
    }
}
