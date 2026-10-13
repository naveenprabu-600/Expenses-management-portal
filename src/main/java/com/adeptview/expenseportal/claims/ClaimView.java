package com.adeptview.expenseportal.claims;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.List;

/**
 * A claim with everything the detail screen shows.
 */
public record ClaimView(
        Long id,
        String title,
        String ownerUsername,
        BigDecimal amount,
        LocalDate expenseDate,
        String notes,
        ClaimStatus status,
        String approverUsername,
        String receiptName,
        String receiptChecksum,
        List<AllocationView> allocations,
        List<EventView> events) {

    public record AllocationView(String costCentre, BigDecimal amount) {
    }

    public record EventView(String action, String actor, String comment, Instant at) {
    }
}
