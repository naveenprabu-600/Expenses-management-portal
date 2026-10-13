package com.adeptview.expenseportal.claims;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

public interface ClaimEventRepository extends JpaRepository<ClaimEvent, Long> {

    List<ClaimEvent> findByClaimIdOrderByOccurredAtAscIdAsc(Long claimId);
}
