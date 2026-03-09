package com.dcaguard.dto.response;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

public class PortfolioResponse {
    private Long id;
    private String name;
    private String baseCurrency;
    private int positionCount;
    private LocalDateTime createdAt;

    public PortfolioResponse() {
        //empty
    }

    public PortfolioResponse(Long id, String name, String baseCurrency, int positionCount, LocalDateTime createdAt) {
        this.id = id;
        this.name = name;
        this.baseCurrency = baseCurrency;
        this.positionCount = positionCount;
        this.createdAt = createdAt;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getBaseCurrency() { return baseCurrency; }
    public void setBaseCurrency(String baseCurrency) { this.baseCurrency = baseCurrency; }
    public int getPositionCount() { return positionCount; }
    public void setPositionCount(int positionCount) { this.positionCount = positionCount; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
}
