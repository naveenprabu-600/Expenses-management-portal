package com.adeptview.expenseportal.approvals;

import java.util.List;
import java.util.Objects;

import com.adeptview.expenseportal.auth.CurrentUser;
import com.adeptview.expenseportal.claims.Claim;
import com.adeptview.expenseportal.claims.ClaimEvent;
import com.adeptview.expenseportal.claims.ClaimEventRepository;
import com.adeptview.expenseportal.claims.ClaimRepository;
import com.adeptview.expenseportal.claims.ClaimService;
import com.adeptview.expenseportal.claims.ClaimStatus;
import com.adeptview.expenseportal.claims.ClaimSummary;
import com.adeptview.expenseportal.claims.ClaimView;
import com.adeptview.expenseportal.common.BadRequestException;
import com.adeptview.expenseportal.common.ConflictException;
import com.adeptview.expenseportal.common.NotFoundException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ApprovalService {

    private static final Logger log = LoggerFactory.getLogger(ApprovalService.class);

    private final ClaimRepository claims;
    private final ClaimEventRepository events;
    private final ClaimService claimService;

    public ApprovalService(ClaimRepository claims, ClaimEventRepository events, ClaimService claimService) {
        this.claims = claims;
        this.events = events;
        this.claimService = claimService;
    }

    @Transactional(readOnly = true)
    public List<ClaimSummary> pending(CurrentUser manager) {
        return claims.findByApproverIdAndStatusOrderByCreatedAtAsc(manager.id(), ClaimStatus.SUBMITTED).stream()
                .map(ClaimSummary::of)
                .toList();
    }

    @Transactional
    public ClaimView approve(long claimId, String comment, CurrentUser manager) {
        return decide(claimId, ClaimStatus.APPROVED, comment, manager);
    }

    @Transactional
    public ClaimView reject(long claimId, String comment, CurrentUser manager) {
        if (comment == null || comment.isBlank()) {
            throw new BadRequestException("Say why the claim is rejected");
        }
        return decide(claimId, ClaimStatus.REJECTED, comment, manager);
    }

    private ClaimView decide(long claimId, ClaimStatus decision, String comment, CurrentUser manager) {
        Claim claim = claims.findById(claimId)
                .orElseThrow(() -> new NotFoundException("Claim " + claimId + " not found"));
        if (!Objects.equals(claim.getApproverId(), manager.id())) {
            throw new AccessDeniedException("Claim " + claimId + " is not waiting for your approval");
        }
        if (claim.getStatus() != ClaimStatus.SUBMITTED) {
            throw new ConflictException("Claim " + claimId + " is already " + claim.getStatus().name().toLowerCase());
        }

        claim.decide(decision);
        String note = comment == null || comment.isBlank() ? null : comment.trim();
        events.save(new ClaimEvent(claimId, decision.name(), manager.username(), note));
        log.info("Claim {} {} by {}", claimId, decision.name().toLowerCase(), manager.username());
        return claimService.view(claim);
    }
}
