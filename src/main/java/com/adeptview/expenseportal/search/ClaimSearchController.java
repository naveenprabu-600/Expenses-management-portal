package com.adeptview.expenseportal.search;

import java.sql.SQLException;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;

import com.adeptview.expenseportal.claims.ClaimStatus;
import com.adeptview.expenseportal.claims.ClaimSummary;
import com.adeptview.expenseportal.common.BadRequestException;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/search")
public class ClaimSearchController {

    private static final int MAX_QUERY_LENGTH = 100;

    private final ClaimSearchRepository search;

    public ClaimSearchController(ClaimSearchRepository search) {
        this.search = search;
    }

    @GetMapping
    public List<ClaimSummary> search(@RequestParam("q") String q) throws SQLException {
        String text = q.trim();
        if (text.isEmpty() || text.length() > MAX_QUERY_LENGTH) {
            throw new BadRequestException("Search text must be 1 to " + MAX_QUERY_LENGTH + " characters");
        }
        return search.search(text);
    }

    @GetMapping("/summary")
    public Map<ClaimStatus, Integer> summary() throws SQLException {
        Map<ClaimStatus, Integer> counts = new EnumMap<>(ClaimStatus.class);
        for (ClaimStatus status : ClaimStatus.values()) {
            counts.put(status, search.countByStatus(status));
        }
        return counts;
    }
}
