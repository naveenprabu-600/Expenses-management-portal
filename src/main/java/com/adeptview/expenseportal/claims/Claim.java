package com.adeptview.expenseportal.claims;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import jakarta.persistence.Basic;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OrderBy;
import jakarta.persistence.Table;

@Entity
@Table(name = "claims")
public class Claim {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "owner_id", nullable = false)
    private Long ownerId;

    @Column(name = "owner_username", nullable = false, length = 100)
    private String ownerUsername;

    @Column(nullable = false, length = 200)
    private String title;

    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal amount;

    @Column(name = "expense_date", nullable = false)
    private LocalDate expenseDate;

    @Column(columnDefinition = "text")
    private String notes;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private ClaimStatus status;

    @Column(name = "approver_id")
    private Long approverId;

    @Column(name = "receipt_name")
    private String receiptName;

    @Column(name = "receipt_checksum", length = 64)
    private String receiptChecksum;

    @Basic(fetch = FetchType.LAZY)
    @Column(name = "receipt_data")
    private byte[] receiptData;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    @OneToMany(mappedBy = "claim", cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("id")
    private List<ClaimAllocation> allocations = new ArrayList<>();

    protected Claim() {
    }

    public Claim(Long ownerId, String ownerUsername, String title, BigDecimal amount, LocalDate expenseDate,
            String notes) {
        this.ownerId = ownerId;
        this.ownerUsername = ownerUsername;
        this.title = title;
        this.amount = amount;
        this.expenseDate = expenseDate;
        this.notes = notes;
        this.status = ClaimStatus.SUBMITTED;
        this.createdAt = Instant.now();
    }

    public void addAllocation(ClaimAllocation allocation) {
        allocation.attachTo(this);
        allocations.add(allocation);
    }

    public void assignApprover(Long approverId) {
        this.approverId = approverId;
    }

    public void decide(ClaimStatus decision) {
        this.status = decision;
    }

    public void attachReceipt(String name, String checksum, byte[] content) {
        this.receiptName = name;
        this.receiptChecksum = checksum;
        this.receiptData = content;
    }

    public Long getId() {
        return id;
    }

    public Long getOwnerId() {
        return ownerId;
    }

    public String getOwnerUsername() {
        return ownerUsername;
    }

    public String getTitle() {
        return title;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public LocalDate getExpenseDate() {
        return expenseDate;
    }

    public String getNotes() {
        return notes;
    }

    public ClaimStatus getStatus() {
        return status;
    }

    public Long getApproverId() {
        return approverId;
    }

    public String getReceiptName() {
        return receiptName;
    }

    public String getReceiptChecksum() {
        return receiptChecksum;
    }

    public byte[] getReceiptData() {
        return receiptData;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public List<ClaimAllocation> getAllocations() {
        return Collections.unmodifiableList(allocations);
    }
}
