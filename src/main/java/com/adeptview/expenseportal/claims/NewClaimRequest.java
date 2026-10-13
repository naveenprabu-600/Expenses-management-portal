package com.adeptview.expenseportal.claims;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PastOrPresent;
import jakarta.validation.constraints.Size;

/**
 * A new claim. When more than one cost centre is given, the amount is shared
 * equally between them.
 */
public record NewClaimRequest(
        @NotBlank @Size(max = 200) String title,
        @NotNull @DecimalMin("0.01") @Digits(integer = 10, fraction = 2) BigDecimal amount,
        @NotNull @PastOrPresent LocalDate expenseDate,
        @Size(max = 2000) String notes,
        @NotEmpty @Size(max = 3) List<@NotBlank String> costCentres) {
}
