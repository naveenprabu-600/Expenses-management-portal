package com.adeptview.expenseportal.common;

/**
 * The body of every error response. {@code referenceId} is the request ID, which
 * the web app shows to the user so it can be quoted in a support ticket.
 */
public record ApiError(String error, String referenceId) {
}
