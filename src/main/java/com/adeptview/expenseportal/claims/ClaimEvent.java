package com.adeptview.expenseportal.claims;

import java.time.Instant;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

/**
 * One step in a claim's history: submitted, imported, approved or rejected.
 */
@Entity
@Table(name = "claim_events")
public class ClaimEvent {

    public static final String SUBMITTED = "SUBMITTED";
    public static final String IMPORTED = "IMPORTED";

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "claim_id", nullable = false)
    private Long claimId;

    @Column(nullable = false, length = 20)
    private String action;

    @Column(name = "actor_username", nullable = false, length = 100)
    private String actorUsername;

    @Column(columnDefinition = "text")
    private String comment;

    @Column(name = "occurred_at", nullable = false)
    private Instant occurredAt;

    protected ClaimEvent() {
    }

    public ClaimEvent(Long claimId, String action, String actorUsername, String comment) {
        this.claimId = claimId;
        this.action = action;
        this.actorUsername = actorUsername;
        this.comment = comment;
        this.occurredAt = Instant.now();
    }

    public Long getClaimId() {
        return claimId;
    }

    public String getAction() {
        return action;
    }

    public String getActorUsername() {
        return actorUsername;
    }

    public String getComment() {
        return comment;
    }

    public Instant getOccurredAt() {
        return occurredAt;
    }
}
