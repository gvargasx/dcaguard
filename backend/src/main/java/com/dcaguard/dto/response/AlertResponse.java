package com.dcaguard.dto.response;

import java.math.BigDecimal;

public class AlertResponse {
    private String type;
    private String severity;
    private String message;
    private BigDecimal currentPrice;
    private BigDecimal referencePrice;
    private BigDecimal changePercent;

    public AlertResponse() {
        //empty
    }

    public AlertResponse(String type, String severity, String message,
                         BigDecimal currentPrice, BigDecimal referencePrice, BigDecimal changePercent) {
        this.type = type;
        this.severity = severity;
        this.message = message;
        this.currentPrice = currentPrice;
        this.referencePrice = referencePrice;
        this.changePercent = changePercent;
    }

    public String getType() { return type; }
    public void setType(String type) { this.type = type; }
    public String getSeverity() { return severity; }
    public void setSeverity(String severity) { this.severity = severity; }
    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }
    public BigDecimal getCurrentPrice() { return currentPrice; }
    public void setCurrentPrice(BigDecimal currentPrice) { this.currentPrice = currentPrice; }
    public BigDecimal getReferencePrice() { return referencePrice; }
    public void setReferencePrice(BigDecimal referencePrice) { this.referencePrice = referencePrice; }
    public BigDecimal getChangePercent() { return changePercent; }
    public void setChangePercent(BigDecimal changePercent) { this.changePercent = changePercent; }
}
