package com.adeptview.expenseportal.claims;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import com.adeptview.expenseportal.approvals.ApprovalRoutingService;
import com.adeptview.expenseportal.auth.CurrentUser;
import com.adeptview.expenseportal.auth.Role;
import com.adeptview.expenseportal.auth.UserRepository;
import com.adeptview.expenseportal.common.BadRequestException;
import com.adeptview.expenseportal.costcentres.CostCentre;
import com.adeptview.expenseportal.costcentres.CostCentreRepository;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.test.util.ReflectionTestUtils;

@ExtendWith(MockitoExtension.class)
class ClaimServiceTest {

    @Mock
    private ClaimRepository claims;

    @Mock
    private ClaimEventRepository events;

    @Mock
    private CostCentreRepository costCentres;

    @Mock
    private UserRepository users;

    @Mock
    private ApprovalRoutingService routing;

    @InjectMocks
    private ClaimService service;

    private final CurrentUser alice = new CurrentUser(1L, "alice", Role.EMPLOYEE);

    @BeforeEach
    void setUp() {
        lenient().when(claims.save(any(Claim.class))).thenAnswer(invocation -> {
            Claim claim = invocation.getArgument(0);
            ReflectionTestUtils.setField(claim, "id", 1001L);
            return claim;
        });
        lenient().when(events.findByClaimIdOrderByOccurredAtAscIdAsc(any())).thenReturn(List.of());
        lenient().when(costCentres.findById("ENG")).thenReturn(Optional.of(new CostCentre("ENG", "Engineering", "ENG")));
        lenient().when(costCentres.findById("ENG-CLOUD"))
                .thenReturn(Optional.of(new CostCentre("ENG-CLOUD", "Engineering - Cloud", "ENG")));
        lenient().when(costCentres.findById("FIN")).thenReturn(Optional.of(new CostCentre("FIN", "Finance", "FIN")));
    }

    @Test
    void chargesTheWholeAmountToASingleCostCentre() {
        ClaimView view = service.submit(request("42.50", "ENG"), alice);

        assertThat(amountsOf(view)).usingElementComparator(BigDecimal::compareTo)
                .containsExactly(new BigDecimal("42.50"));
        assertThat(view.status()).isEqualTo(ClaimStatus.SUBMITTED);
        verify(routing).route(any(Claim.class));
    }

    @Test
    void sharesTheAmountBetweenTwoCostCentres() {
        ClaimView view = service.submit(request("100.00", "ENG", "ENG-CLOUD"), alice);

        assertThat(amountsOf(view)).usingElementComparator(BigDecimal::compareTo)
                .containsExactly(new BigDecimal("50.00"), new BigDecimal("50.00"));
    }

    @Test
    void sharesTheAmountBetweenThreeCostCentres() {
        ClaimView view = service.submit(request("90.00", "ENG", "ENG-CLOUD", "FIN"), alice);

        assertThat(amountsOf(view)).usingElementComparator(BigDecimal::compareTo)
                .containsExactly(new BigDecimal("30.00"), new BigDecimal("30.00"), new BigDecimal("30.00"));
    }

    @Test
    void refusesAnUnknownCostCentre() {
        when(costCentres.findById("XYZ")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.submit(request("10.00", "XYZ"), alice))
                .isInstanceOf(BadRequestException.class)
                .hasMessageContaining("XYZ");
        verify(claims, never()).save(any());
    }

    @Test
    void hidesAClaimFromOtherEmployees() {
        Claim someoneElses = new Claim(2L, "bob", "Hotel", new BigDecimal("80.00"), LocalDate.of(2026, 10, 1), null);
        ReflectionTestUtils.setField(someoneElses, "id", 1002L);
        when(claims.findById(1002L)).thenReturn(Optional.of(someoneElses));

        assertThatThrownBy(() -> service.get(1002L, alice)).isInstanceOf(AccessDeniedException.class);
    }

    private static NewClaimRequest request(String amount, String... costCentres) {
        return new NewClaimRequest("Taxi", new BigDecimal(amount), LocalDate.of(2026, 10, 1), null,
                List.of(costCentres));
    }

    private static List<BigDecimal> amountsOf(ClaimView view) {
        return view.allocations().stream().map(ClaimView.AllocationView::amount).toList();
    }
}
