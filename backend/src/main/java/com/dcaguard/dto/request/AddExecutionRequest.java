package com.dcaguard.dto.request;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;
import java.time.LocalDate;

public class AddExecutionRequest {

    @NotNull(message = "Execution date is required")
    private LocalDate executionDate;

    @NotNull(message = "Amount paid is required")
    @DecimalMin(value = "0.01", message = "Amount must be positive")
    private BigDecimal amountPaid;

    @NotNull(message = "Price at execution is required")
    @DecimalMin(value = "0.00000001", message = "Price must be positive")
    private BigDecimal priceAtExec;

    public AddExecutionRequest() { // empty
    }

    public LocalDate getExecutionDate() {
        return executionDate;
    }

    public void setExecutionDate(LocalDate executionDate) {
        this.executionDate = executionDate;
    }

    public BigDecimal getAmountPaid() {
        return amountPaid;
    }

    public void setAmountPaid(BigDecimal amountPaid) {
        this.amountPaid = amountPaid;
    }

    public BigDecimal getPriceAtExec() {
        return priceAtExec;
    }

    public void setPriceAtExec(BigDecimal priceAtExec) {
        this.priceAtExec = priceAtExec;
    }
}
