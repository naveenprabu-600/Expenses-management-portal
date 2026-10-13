package com.adeptview.expenseportal.imports;

import java.util.List;

public record ImportResult(int imported, List<Long> claimIds) {
}
