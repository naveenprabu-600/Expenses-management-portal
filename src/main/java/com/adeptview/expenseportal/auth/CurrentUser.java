package com.adeptview.expenseportal.auth;

import org.springframework.security.oauth2.jwt.Jwt;

/**
 * The signed-in user, as carried in the access token.
 */
public record CurrentUser(long id, String username, Role role) {

    public static CurrentUser from(Jwt jwt) {
        Number id = jwt.getClaim(TokenService.USER_ID_CLAIM);
        Role role = Role.valueOf(jwt.getClaimAsStringList(TokenService.ROLES_CLAIM).get(0));
        return new CurrentUser(id.longValue(), jwt.getSubject(), role);
    }

    public boolean is(Role expected) {
        return role == expected;
    }
}
