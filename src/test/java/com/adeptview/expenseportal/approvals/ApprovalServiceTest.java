package com.adeptview.expenseportal.approvals;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Optional;

import com.adeptview.expenseportal.auth.CurrentUser;
import com.adeptview.expenseportal.auth.Role;
import com.adeptview.expenseportal.claims.Claim;
import com.adeptview.expenseportal.claims.ClaimEvent;
import com.adeptview.expenseportal.claims.ClaimEventRepository;
import com.adeptview.expenseportal.claims.ClaimRepository;
import com.adeptview.expenseportal.claims.ClaimService;
import com.adeptview.expenseportal.claims.ClaimStatus;
import com.adeptview.expenseportal.common.BadRequestException;
import com.adeptview.expenseportal.common.ConflictException;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.test.util.ReflectionTestUtils;

@ExtendWith(MockitoExtension.class)
class ApprovalServiceTest {

    @Mock
    private ClaimRepository claims;

    @Mock
    private ClaimEventRepository events;

    @Mock
    private ClaimService claimService;

    @InjectMocks
    private ApprovalService approvals;

    private final CurrentUser nina = new CurrentUser(4L, "nina", Role.MANAGER);

    @Test
    void theAssignedManagerApproves() {
        Claim claim = claimWaitingFor(4L);
        when(claims.findById(1001L)).thenReturn(Optional.of(claim));

        approvals.approve(1001L, null, nina);

        assertThat(claim.getStatus()).isEqualTo(ClaimStatus.APPROVED);
        verify(events).save(any(ClaimEvent.class));
    }

    @Test
    void anotherManagerCannotDecide() {
        when(claims.findById(1001L)).thenReturn(Optional.of(claimWaitingFor(3L)));

        assertThatThrownBy(() -> approvals.approve(1001L, null, nina)).isInstanceOf(AccessDeniedException.class);
    }

    @Test
    void aRejectionNeedsAReason() {
        assertThatThrownBy(() -> approvals.reject(1001L, "  ", nina)).isInstanceOf(BadRequestException.class);
    }

    @Test
    void aDecidedClaimCannotBeDecidedAgain() {
        Claim claim = claimWaitingFor(4L);
        claim.decide(ClaimStatus.APPROVED);
        when(claims.findById(1001L)).thenReturn(Optional.of(claim));

        assertThatThrownBy(() -> approvals.reject(1001L, "Duplicate", nina)).isInstanceOf(ConflictException.class);
    }

    private static Claim claimWaitingFor(long approverId) {
        Claim claim = new Claim(1L, "alice", "Taxi", new BigDecimal("20.00"), LocalDate.of(2026, 10, 1), null);
        ReflectionTestUtils.setField(claim, "id", 1001L);
        claim.assignApprover(approverId);
        return claim;
    }
}
