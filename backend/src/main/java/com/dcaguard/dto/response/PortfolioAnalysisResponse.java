package com.dcaguard.dto.response;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

public class PortfolioAnalysisResponse {
    private BigDecimal totalValue;
    private BigDecimal totalPnl;
    private BigDecimal totalPnlPercent;
    private int riskScore;
    private String riskLevel;
    private List<PositionResponse> positions;
    private List<AllocationItem> allocationByAsset;
    private List<AllocationItem> allocationByCategory;
    private RiskBreakdown riskBreakdown;
    private List<String> insights;

    public PortfolioAnalysisResponse() {
        //empty
    }

    public BigDecimal getTotalValue() { return totalValue; }
    public void setTotalValue(BigDecimal totalValue) { this.totalValue = totalValue; }
    public BigDecimal getTotalPnl() { return totalPnl; }
    public void setTotalPnl(BigDecimal totalPnl) { this.totalPnl = totalPnl; }
    public BigDecimal getTotalPnlPercent() { return totalPnlPercent; }
    public void setTotalPnlPercent(BigDecimal totalPnlPercent) { this.totalPnlPercent = totalPnlPercent; }
    public int getRiskScore() { return riskScore; }
    public void setRiskScore(int riskScore) { this.riskScore = riskScore; }
    public String getRiskLevel() { return riskLevel; }
    public void setRiskLevel(String riskLevel) { this.riskLevel = riskLevel; }
    public List<PositionResponse> getPositions() { return positions; }
    public void setPositions(List<PositionResponse> positions) { this.positions = positions; }
    public List<AllocationItem> getAllocationByAsset() { return allocationByAsset; }
    public void setAllocationByAsset(List<AllocationItem> allocationByAsset) { this.allocationByAsset = allocationByAsset; }
    public List<AllocationItem> getAllocationByCategory() { return allocationByCategory; }
    public void setAllocationByCategory(List<AllocationItem> allocationByCategory) { this.allocationByCategory = allocationByCategory; }
    public RiskBreakdown getRiskBreakdown() { return riskBreakdown; }
    public void setRiskBreakdown(RiskBreakdown riskBreakdown) { this.riskBreakdown = riskBreakdown; }
    public List<String> getInsights() { return insights; }
    public void setInsights(List<String> insights) { this.insights = insights; }

    public static class AllocationItem {
        private String label;
        private BigDecimal value;
        private BigDecimal percent;

        public AllocationItem() {}

        public AllocationItem(String label, BigDecimal value, BigDecimal percent) {
            this.label = label;
            this.value = value;
            this.percent = percent;
        }

        public String getLabel() { return label; }
        public void setLabel(String label) { this.label = label; }
        public BigDecimal getValue() { return value; }
        public void setValue(BigDecimal value) { this.value = value; }
        public BigDecimal getPercent() { return percent; }
        public void setPercent(BigDecimal percent) { this.percent = percent; }
    }

    public static class RiskBreakdown {
        private int concentrationScore;
        private int categoryRiskScore;
        private int volatilityScore;
        private int drawdownScore;

        public RiskBreakdown() {}

        public int getConcentrationScore() { return concentrationScore; }
        public void setConcentrationScore(int concentrationScore) { this.concentrationScore = concentrationScore; }
        public int getCategoryRiskScore() { return categoryRiskScore; }
        public void setCategoryRiskScore(int categoryRiskScore) { this.categoryRiskScore = categoryRiskScore; }
        public int getVolatilityScore() { return volatilityScore; }
        public void setVolatilityScore(int volatilityScore) { this.volatilityScore = volatilityScore; }
        public int getDrawdownScore() { return drawdownScore; }
        public void setDrawdownScore(int drawdownScore) { this.drawdownScore = drawdownScore; }
    }
}
