package com.dcaguard.dto.request;

import jakarta.validation.constraints.*;
import java.math.BigDecimal;
import java.time.LocalDate;

public class CreateDcaPlanRequest {

    @NotBlank(message = "Asset symbol is required")
    private String symbol;

    @NotNull(message = "Amount is required")
    @DecimalMin(value = "1.00", message = "Minimum amount is 1.00")
    private BigDecimal amount;

    @NotBlank(message = "Frequency is required")
    @Pattern(regexp = "^(DAILY|WEEKLY|BIWEEKLY|MONTHLY)$", message = "Invalid frequency")
    private String frequency;

    @Pattern(regexp = "^(USD|BRL|EUR)$", message = "Currency must be USD, BRL, or EUR")
    private String baseCurrency = "USD";

    @NotNull(message = "Start date is required")
    private LocalDate startDate;

    private LocalDate endDate;

    public CreateDcaPlanRequest() {
        //empty
    }

    public String getSymbol() { return symbol; }
    public void setSymbol(String symbol) { this.symbol = symbol; }
    public BigDecimal getAmount() { return amount; }
    public void setAmount(BigDecimal amount) { this.amount = amount; }
    public String getFrequency() { return frequency; }
    public void setFrequency(String frequency) { this.frequency = frequency; }
    public String getBaseCurrency() { return baseCurrency; }
    public void setBaseCurrency(String baseCurrency) { this.baseCurrency = baseCurrency; }
    public LocalDate getStartDate() { return startDate; }
    public void setStartDate(LocalDate startDate) { this.startDate = startDate; }
    public LocalDate getEndDate() { return endDate; }
    public void setEndDate(LocalDate endDate) { this.endDate = endDate; }
}
