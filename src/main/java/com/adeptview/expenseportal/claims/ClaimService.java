package com.adeptview.expenseportal.claims;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Objects;

import com.adeptview.expenseportal.approvals.ApprovalRoutingService;
import com.adeptview.expenseportal.auth.CurrentUser;
import com.adeptview.expenseportal.auth.Role;
import com.adeptview.expenseportal.auth.User;
import com.adeptview.expenseportal.auth.UserRepository;
import com.adeptview.expenseportal.common.BadRequestException;
import com.adeptview.expenseportal.common.NotFoundException;
import com.adeptview.expenseportal.costcentres.CostCentre;
import com.adeptview.expenseportal.costcentres.CostCentreRepository;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ClaimService {

    private static final Logger log = LoggerFactory.getLogger(ClaimService.class);
    private static final int MAX_TITLE_LENGTH = 200;

    private final ClaimRepository claims;
    private final ClaimEventRepository events;
    private final CostCentreRepository costCentres;
    private final UserRepository users;
    private final ApprovalRoutingService routing;

    public ClaimService(ClaimRepository claims, ClaimEventRepository events, CostCentreRepository costCentres,
            UserRepository users, ApprovalRoutingService routing) {
        this.claims = claims;
        this.events = events;
        this.costCentres = costCentres;
        this.users = users;
        this.routing = routing;
    }

    @Transactional(readOnly = true)
    public List<ClaimSummary> mine(CurrentUser user) {
        return claims.findByOwnerIdOrderByCreatedAtDesc(user.id()).stream().map(ClaimSummary::of).toList();
    }

    @Transactional
    public ClaimView submit(NewClaimRequest request, CurrentUser user) {
        List<CostCentre> centres = resolveCostCentres(request.costCentres());

        Claim claim = new Claim(user.id(), user.username(), request.title().trim(), request.amount(),
                request.expenseDate(), request.notes());
        splitEqually(request.amount(), centres).forEach(claim::addAllocation);
        claims.save(claim);

        events.save(new ClaimEvent(claim.getId(), ClaimEvent.SUBMITTED, user.username(), null));
        routing.route(claim);

        log.info("Claim {} submitted by {} for {} across {} cost centre(s)",
                claim.getId(), user.username(), claim.getAmount(), centres.size());
        return view(claim);
    }

    /**
     * Records a claim brought over from the old expense system. Imported claims
     * are charged to a single cost centre and go through the same routing.
     */
    @Transactional
    public Long recordImported(User owner, String title, BigDecimal amount, LocalDate expenseDate,
            String costCentreCode, String importedBy) {
        CostCentre centre = resolveCostCentres(List.of(costCentreCode)).get(0);
        String safeTitle = title.length() > MAX_TITLE_LENGTH ? title.substring(0, MAX_TITLE_LENGTH) : title;

        Claim claim = new Claim(owner.getId(), owner.getUsername(), safeTitle, amount, expenseDate, null);
        claim.addAllocation(new ClaimAllocation(centre.getCode(), amount));
        claims.save(claim);

        events.save(new ClaimEvent(claim.getId(), ClaimEvent.IMPORTED, importedBy, "Imported from the previous expense system"));
        routing.route(claim);
        return claim.getId();
    }

    @Transactional(readOnly = true)
    public ClaimView get(long id, CurrentUser user) {
        Claim claim = claims.findById(id).orElseThrow(() -> new NotFoundException("Claim " + id + " not found"));
        ensureCanView(claim, user);
        return view(claim);
    }

    /**
     * The claimant, the claim's approver and finance may see a claim.
     */
    public void ensureCanView(Claim claim, CurrentUser user) {
        boolean allowed = Objects.equals(claim.getOwnerId(), user.id())
                || Objects.equals(claim.getApproverId(), user.id())
                || user.is(Role.FINANCE);
        if (!allowed) {
            throw new AccessDeniedException("You do not have access to claim " + claim.getId());
        }
    }

    public ClaimView view(Claim claim) {
        String approver = claim.getApproverId() == null
                ? null
                : users.findById(claim.getApproverId()).map(User::getUsername).orElse(null);

        List<ClaimView.AllocationView> allocations = claim.getAllocations().stream()
                .map(allocation -> new ClaimView.AllocationView(allocation.getCostCentreCode(), allocation.getAmount()))
                .toList();

        List<ClaimView.EventView> history = events.findByClaimIdOrderByOccurredAtAscIdAsc(claim.getId()).stream()
                .map(event -> new ClaimView.EventView(event.getAction(), event.getActorUsername(), event.getComment(),
                        event.getOccurredAt()))
                .toList();

        return new ClaimView(claim.getId(), claim.getTitle(), claim.getOwnerUsername(), claim.getAmount(),
                claim.getExpenseDate(), claim.getNotes(), claim.getStatus(), approver, claim.getReceiptName(),
                claim.getReceiptChecksum(), allocations, history);
    }

    private List<ClaimAllocation> splitEqually(BigDecimal amount, List<CostCentre> centres) {
        BigDecimal share = amount.divide(BigDecimal.valueOf(centres.size()));
        List<ClaimAllocation> allocations = new ArrayList<>();
        for (CostCentre centre : centres) {
            allocations.add(new ClaimAllocation(centre.getCode(), share));
        }
        return allocations;
    }

    private List<CostCentre> resolveCostCentres(List<String> codes) {
        List<CostCentre> resolved = new ArrayList<>();
        for (String code : new LinkedHashSet<>(codes)) {
            String trimmed = code.trim();
            resolved.add(costCentres.findById(trimmed)
                    .orElseThrow(() -> new BadRequestException("Unknown cost centre '" + trimmed + "'")));
        }
        return resolved;
    }
}
