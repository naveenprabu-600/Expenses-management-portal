package com.adeptview.expenseportal.claims;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * A claim as shown in a list.
 */
public record ClaimSummary(
        Long id,
        String title,
        String ownerUsername,
        BigDecimal amount,
        ClaimStatus status,
        LocalDate expenseDate) {

    public static ClaimSummary of(Claim claim) {
        return new ClaimSummary(claim.getId(), claim.getTitle(), claim.getOwnerUsername(), claim.getAmount(),
                claim.getStatus(), claim.getExpenseDate());
    }
}
