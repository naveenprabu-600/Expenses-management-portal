package com.adeptview.expenseportal.approvals;

import java.util.List;

import jakarta.validation.Valid;

import com.adeptview.expenseportal.auth.CurrentUser;
import com.adeptview.expenseportal.claims.ClaimSummary;
import com.adeptview.expenseportal.claims.ClaimView;

import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/approvals")
public class ApprovalController {

    private final ApprovalService approvals;

    public ApprovalController(ApprovalService approvals) {
        this.approvals = approvals;
    }

    @GetMapping
    public List<ClaimSummary> pending(@AuthenticationPrincipal Jwt jwt) {
        return approvals.pending(CurrentUser.from(jwt));
    }

    @PostMapping("/{id}/approve")
    public ClaimView approve(@PathVariable long id, @Valid @RequestBody(required = false) DecisionRequest request,
            @AuthenticationPrincipal Jwt jwt) {
        return approvals.approve(id, request == null ? null : request.comment(), CurrentUser.from(jwt));
    }

    @PostMapping("/{id}/reject")
    public ClaimView reject(@PathVariable long id, @Valid @RequestBody DecisionRequest request,
            @AuthenticationPrincipal Jwt jwt) {
        return approvals.reject(id, request.comment(), CurrentUser.from(jwt));
    }
}
