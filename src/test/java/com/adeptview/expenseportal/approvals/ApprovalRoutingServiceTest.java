package com.adeptview.expenseportal.approvals;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Optional;

import com.adeptview.expenseportal.auth.Role;
import com.adeptview.expenseportal.auth.User;
import com.adeptview.expenseportal.auth.UserRepository;
import com.adeptview.expenseportal.claims.Claim;
import com.adeptview.expenseportal.claims.ClaimAllocation;
import com.adeptview.expenseportal.costcentres.CostCentre;
import com.adeptview.expenseportal.costcentres.CostCentreRepository;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

@ExtendWith(MockitoExtension.class)
class ApprovalRoutingServiceTest {

    @Mock
    private CostCentreRepository costCentres;

    @Mock
    private UserRepository users;

    @InjectMocks
    private ApprovalRoutingService routing;

    @Test
    void sendsAFinanceClaimToTheFinanceManager() {
        when(costCentres.findById("FIN")).thenReturn(Optional.of(new CostCentre("FIN", "Finance", "FIN")));
        when(users.findFirstByDepartmentCodeAndRole("FIN", Role.MANAGER)).thenReturn(Optional.of(manager("nina", 4L, "FIN")));
        Claim claim = claimChargedTo("FIN");

        routing.route(claim);

        assertThat(claim.getApproverId()).isEqualTo(4L);
    }

    @Test
    void sendsAnEngineeringClaimToTheEngineeringManager() {
        when(costCentres.findById("ENG")).thenReturn(Optional.of(new CostCentre("ENG", "Engineering", "ENG")));
        when(users.findFirstByDepartmentCodeAndRole("ENG", Role.MANAGER)).thenReturn(Optional.of(manager("mark", 3L, "ENG")));
        Claim claim = claimChargedTo("ENG");

        routing.route(claim);

        assertThat(claim.getApproverId()).isEqualTo(3L);
    }

    @Test
    void leavesTheClaimUnassignedWhenTheDepartmentHasNoManager() {
        when(costCentres.findById("OPS")).thenReturn(Optional.of(new CostCentre("OPS", "Operations", "OPS")));
        when(users.findFirstByDepartmentCodeAndRole("OPS", Role.MANAGER)).thenReturn(Optional.empty());
        Claim claim = claimChargedTo("OPS");

        routing.route(claim);

        assertThat(claim.getApproverId()).isNull();
    }

    private static Claim claimChargedTo(String costCentre) {
        Claim claim = new Claim(1L, "alice", "Taxi", new BigDecimal("20.00"), LocalDate.of(2026, 10, 1), null);
        claim.addAllocation(new ClaimAllocation(costCentre, new BigDecimal("20.00")));
        ReflectionTestUtils.setField(claim, "id", 1001L);
        return claim;
    }

    private static User manager(String username, long id, String department) {
        User user = new User(username, username, "unused", Role.MANAGER, department);
        ReflectionTestUtils.setField(user, "id", id);
        return user;
    }
}
