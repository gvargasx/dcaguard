package com.dcaguard.dto.request;

import jakarta.validation.constraints.*;
import java.math.BigDecimal;

public class AddPositionRequest {

    @NotBlank(message = "Asset symbol is required")
    private String symbol;

    @NotNull(message = "Quantity is required")
    @DecimalMin(value = "0.000000000001", message = "Quantity must be positive")
    private BigDecimal quantity;

    @DecimalMin(value = "0", message = "Average buy price must be non-negative")
    private BigDecimal avgBuyPrice;

    private String notes;

    public AddPositionRequest() {
        // empty
    }

    public String getSymbol() { return symbol; }
    public void setSymbol(String symbol) { this.symbol = symbol; }
    public BigDecimal getQuantity() { return quantity; }
    public void setQuantity(BigDecimal quantity) { this.quantity = quantity; }
    public BigDecimal getAvgBuyPrice() { return avgBuyPrice; }
    public void setAvgBuyPrice(BigDecimal avgBuyPrice) { this.avgBuyPrice = avgBuyPrice; }
    public String getNotes() { return notes; }
    public void setNotes(String notes) { this.notes = notes; }
}
