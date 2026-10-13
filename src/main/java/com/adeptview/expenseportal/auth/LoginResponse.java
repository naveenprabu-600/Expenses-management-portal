package com.adeptview.expenseportal.auth;

public record LoginResponse(String token, String username, String displayName, Role role) {
}
