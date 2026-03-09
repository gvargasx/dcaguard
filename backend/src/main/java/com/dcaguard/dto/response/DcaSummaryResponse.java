package com.dcaguard.dto.response;

import java.math.BigDecimal;
import java.util.List;

public class DcaSummaryResponse {
    private Long planId;
    private String symbol;
    private BigDecimal totalInvested;
    private BigDecimal totalQuantity;
    private BigDecimal averagePrice;
    private BigDecimal currentPrice;
    private BigDecimal currentValue;
    private BigDecimal pnl;
    private BigDecimal pnlPercent;
    private int totalExecutions;
    private List<DcaChartPoint> chartData;
    private List<ExecutionResponse> executions;

    public DcaSummaryResponse() {
        //empty
    }

    public Long getPlanId() { return planId; }
    public void setPlanId(Long planId) { this.planId = planId; }
    public String getSymbol() { return symbol; }
    public void setSymbol(String symbol) { this.symbol = symbol; }
    public BigDecimal getTotalInvested() { return totalInvested; }
    public void setTotalInvested(BigDecimal totalInvested) { this.totalInvested = totalInvested; }
    public BigDecimal getTotalQuantity() { return totalQuantity; }
    public void setTotalQuantity(BigDecimal totalQuantity) { this.totalQuantity = totalQuantity; }
    public BigDecimal getAveragePrice() { return averagePrice; }
    public void setAveragePrice(BigDecimal averagePrice) { this.averagePrice = averagePrice; }
    public BigDecimal getCurrentPrice() { return currentPrice; }
    public void setCurrentPrice(BigDecimal currentPrice) { this.currentPrice = currentPrice; }
    public BigDecimal getCurrentValue() { return currentValue; }
    public void setCurrentValue(BigDecimal currentValue) { this.currentValue = currentValue; }
    public BigDecimal getPnl() { return pnl; }
    public void setPnl(BigDecimal pnl) { this.pnl = pnl; }
    public BigDecimal getPnlPercent() { return pnlPercent; }
    public void setPnlPercent(BigDecimal pnlPercent) { this.pnlPercent = pnlPercent; }
    public int getTotalExecutions() { return totalExecutions; }
    public void setTotalExecutions(int totalExecutions) { this.totalExecutions = totalExecutions; }
    public List<DcaChartPoint> getChartData() { return chartData; }
    public void setChartData(List<DcaChartPoint> chartData) { this.chartData = chartData; }
    public List<ExecutionResponse> getExecutions() { return executions; }
    public void setExecutions(List<ExecutionResponse> executions) { this.executions = executions; }

    public static class DcaChartPoint {
        private String date;
        private BigDecimal totalInvested;
        private BigDecimal portfolioValue;
        private BigDecimal averagePrice;
        private BigDecimal marketPrice;

        public DcaChartPoint() {}

        public DcaChartPoint(String date, BigDecimal totalInvested, BigDecimal portfolioValue,
                             BigDecimal averagePrice, BigDecimal marketPrice) {
            this.date = date;
            this.totalInvested = totalInvested;
            this.portfolioValue = portfolioValue;
            this.averagePrice = averagePrice;
            this.marketPrice = marketPrice;
        }

        public String getDate() { return date; }
        public void setDate(String date) { this.date = date; }
        public BigDecimal getTotalInvested() { return totalInvested; }
        public void setTotalInvested(BigDecimal totalInvested) { this.totalInvested = totalInvested; }
        public BigDecimal getPortfolioValue() { return portfolioValue; }
        public void setPortfolioValue(BigDecimal portfolioValue) { this.portfolioValue = portfolioValue; }
        public BigDecimal getAveragePrice() { return averagePrice; }
        public void setAveragePrice(BigDecimal averagePrice) { this.averagePrice = averagePrice; }
        public BigDecimal getMarketPrice() { return marketPrice; }
        public void setMarketPrice(BigDecimal marketPrice) { this.marketPrice = marketPrice; }
    }

    public static class ExecutionResponse {
        private Long id;
        private String date;
        private BigDecimal amountPaid;
        private BigDecimal priceAtExec;
        private BigDecimal quantity;
        private boolean simulated;

        public ExecutionResponse() {}

        public Long getId() { return id; }
        public void setId(Long id) { this.id = id; }
        public String getDate() { return date; }
        public void setDate(String date) { this.date = date; }
        public BigDecimal getAmountPaid() { return amountPaid; }
        public void setAmountPaid(BigDecimal amountPaid) { this.amountPaid = amountPaid; }
        public BigDecimal getPriceAtExec() { return priceAtExec; }
        public void setPriceAtExec(BigDecimal priceAtExec) { this.priceAtExec = priceAtExec; }
        public BigDecimal getQuantity() { return quantity; }
        public void setQuantity(BigDecimal quantity) { this.quantity = quantity; }
        public boolean isSimulated() { return simulated; }
        public void setSimulated(boolean simulated) { this.simulated = simulated; }
    }
}
