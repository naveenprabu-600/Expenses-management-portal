package com.adeptview.expenseportal.receipts;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.util.Optional;

import com.adeptview.expenseportal.auth.CurrentUser;
import com.adeptview.expenseportal.auth.Role;
import com.adeptview.expenseportal.claims.Claim;
import com.adeptview.expenseportal.claims.ClaimRepository;
import com.adeptview.expenseportal.claims.ClaimService;
import com.adeptview.expenseportal.common.ConflictException;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.test.util.ReflectionTestUtils;

@ExtendWith(MockitoExtension.class)
class ReceiptServiceTest {

    @Mock
    private ClaimRepository claims;

    @Mock
    private ClaimService claimService;

    @InjectMocks
    private ReceiptService receipts;

    private final CurrentUser alice = new CurrentUser(1L, "alice", Role.EMPLOYEE);

    @Test
    void theSameContentGivesTheSameFingerprint() throws Exception {
        byte[] content = "Taxi receipt 42.50".getBytes(StandardCharsets.UTF_8);

        assertThat(receipts.checksum(content)).isEqualTo(receipts.checksum(content.clone()));
    }

    @Test
    void differentContentGivesADifferentFingerprint() throws Exception {
        assertThat(receipts.checksum("Taxi 42.50".getBytes(StandardCharsets.UTF_8)))
                .isNotEqualTo(receipts.checksum("Taxi 42.60".getBytes(StandardCharsets.UTF_8)));
    }

    @Test
    void attachesAReceiptToTheClaimantsOwnClaim() throws Exception {
        Claim claim = claimOwnedBy(1L, 1002L);
        when(claims.findById(1002L)).thenReturn(Optional.of(claim));
        when(claims.findFirstByReceiptChecksumAndIdNot(anyString(), eq(1002L))).thenReturn(Optional.empty());

        receipts.attach(1002L, pdf("taxi.pdf"), alice);

        assertThat(claim.getReceiptName()).isEqualTo("taxi.pdf");
        assertThat(claim.getReceiptChecksum()).isNotBlank();
    }

    @Test
    void aReceiptCannotBeClaimedTwice() {
        when(claims.findById(1002L)).thenReturn(Optional.of(claimOwnedBy(1L, 1002L)));
        when(claims.findFirstByReceiptChecksumAndIdNot(anyString(), eq(1002L)))
                .thenReturn(Optional.of(claimOwnedBy(1L, 1001L)));

        assertThatThrownBy(() -> receipts.attach(1002L, pdf("taxi.pdf"), alice))
                .isInstanceOf(ConflictException.class)
                .hasMessageContaining("1001");
    }

    @Test
    void onlyTheClaimantAttachesAReceipt() {
        when(claims.findById(1002L)).thenReturn(Optional.of(claimOwnedBy(2L, 1002L)));

        assertThatThrownBy(() -> receipts.attach(1002L, pdf("taxi.pdf"), alice))
                .isInstanceOf(AccessDeniedException.class);
    }

    private static MockMultipartFile pdf(String name) {
        return new MockMultipartFile("file", name, "application/pdf", "%PDF-1.7 taxi".getBytes(StandardCharsets.UTF_8));
    }

    private static Claim claimOwnedBy(long ownerId, long claimId) {
        Claim claim = new Claim(ownerId, "alice", "Taxi", new BigDecimal("42.50"), LocalDate.of(2026, 10, 1), null);
        ReflectionTestUtils.setField(claim, "id", claimId);
        return claim;
    }
}
