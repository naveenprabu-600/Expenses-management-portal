package com.adeptview.expenseportal.claims;

import java.math.BigDecimal;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

/**
 * The part of a claim charged to one cost centre.
 */
@Entity
@Table(name = "claim_allocations")
public class ClaimAllocation {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "claim_id")
    private Claim claim;

    @Column(name = "cost_centre_code", nullable = false, length = 30)
    private String costCentreCode;

    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal amount;

    protected ClaimAllocation() {
    }

    public ClaimAllocation(String costCentreCode, BigDecimal amount) {
        this.costCentreCode = costCentreCode;
        this.amount = amount;
    }

    void attachTo(Claim owner) {
        this.claim = owner;
    }

    public String getCostCentreCode() {
        return costCentreCode;
    }

    public BigDecimal getAmount() {
        return amount;
    }
}
