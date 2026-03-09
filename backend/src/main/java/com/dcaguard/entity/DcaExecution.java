package com.dcaguard.entity;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "dca_executions")
public class DcaExecution {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "plan_id", nullable = false)
    private DcaPlan plan;

    @Column(name = "execution_date", nullable = false)
    private LocalDate executionDate;

    @Column(name = "amount_paid", nullable = false, precision = 18, scale = 2)
    private BigDecimal amountPaid;

    @Column(name = "price_at_exec", nullable = false, precision = 24, scale = 8)
    private BigDecimal priceAtExec;

    @Column(nullable = false, precision = 24, scale = 12)
    private BigDecimal quantity;

    @Column(name = "is_simulated", nullable = false)
    private boolean simulated = true;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    public DcaExecution() {
        //empty
    }

    public DcaExecution(DcaPlan plan, LocalDate executionDate, BigDecimal amountPaid,
                        BigDecimal priceAtExec, BigDecimal quantity, boolean simulated) {
        this.plan = plan;
        this.executionDate = executionDate;
        this.amountPaid = amountPaid;
        this.priceAtExec = priceAtExec;
        this.quantity = quantity;
        this.simulated = simulated;
    }

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public DcaPlan getPlan() { return plan; }
    public void setPlan(DcaPlan plan) { this.plan = plan; }
    public LocalDate getExecutionDate() { return executionDate; }
    public void setExecutionDate(LocalDate executionDate) { this.executionDate = executionDate; }
    public BigDecimal getAmountPaid() { return amountPaid; }
    public void setAmountPaid(BigDecimal amountPaid) { this.amountPaid = amountPaid; }
    public BigDecimal getPriceAtExec() { return priceAtExec; }
    public void setPriceAtExec(BigDecimal priceAtExec) { this.priceAtExec = priceAtExec; }
    public BigDecimal getQuantity() { return quantity; }
    public void setQuantity(BigDecimal quantity) { this.quantity = quantity; }
    public boolean isSimulated() { return simulated; }
    public void setSimulated(boolean simulated) { this.simulated = simulated; }
    public LocalDateTime getCreatedAt() { return createdAt; }
}
