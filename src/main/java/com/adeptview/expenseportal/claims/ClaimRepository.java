package com.adeptview.expenseportal.claims;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

public interface ClaimRepository extends JpaRepository<Claim, Long> {

    List<Claim> findByOwnerIdOrderByCreatedAtDesc(Long ownerId);

    List<Claim> findByApproverIdAndStatusOrderByCreatedAtAsc(Long approverId, ClaimStatus status);

    Optional<Claim> findFirstByReceiptChecksumAndIdNot(String receiptChecksum, Long id);
}
