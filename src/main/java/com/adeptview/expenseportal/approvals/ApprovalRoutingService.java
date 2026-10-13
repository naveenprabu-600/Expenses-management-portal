package com.adeptview.expenseportal.approvals;

import java.util.Optional;

import com.adeptview.expenseportal.auth.Role;
import com.adeptview.expenseportal.auth.User;
import com.adeptview.expenseportal.auth.UserRepository;
import com.adeptview.expenseportal.claims.Claim;
import com.adeptview.expenseportal.common.NotFoundException;
import com.adeptview.expenseportal.costcentres.CostCentre;
import com.adeptview.expenseportal.costcentres.CostCentreRepository;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

/**
 * Decides who approves a claim: the manager of the department that owns the
 * claim's primary (first) cost centre.
 */
@Service
public class ApprovalRoutingService {

    private static final Logger log = LoggerFactory.getLogger(ApprovalRoutingService.class);

    private final CostCentreRepository costCentres;
    private final UserRepository users;

    public ApprovalRoutingService(CostCentreRepository costCentres, UserRepository users) {
        this.costCentres = costCentres;
        this.users = users;
    }

    public void route(Claim claim) {
        String primaryCode = claim.getAllocations().get(0).getCostCentreCode();
        CostCentre centre = costCentres.findById(primaryCode)
                .orElseThrow(() -> new NotFoundException("Cost centre " + primaryCode + " not found"));

        Optional<User> approver = users.findFirstByDepartmentCodeAndRole(centre.getCode(), Role.MANAGER);
        if (approver.isEmpty()) {
            log.warn("No approver found for department '{}'; claim {} left unassigned", centre.getCode(), claim.getId());
            return;
        }

        claim.assignApprover(approver.get().getId());
        log.info("Claim {} routed to {} for approval", claim.getId(), approver.get().getUsername());
    }
}
