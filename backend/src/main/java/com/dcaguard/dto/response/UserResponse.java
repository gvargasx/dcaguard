package com.dcaguard.dto.response;

public class UserResponse {
    private Long id;
    private String email;
    private String displayName;
    private String planType;
    private boolean hasAds;
    private int maxPortfolios;
    private int maxDcaPlans;
    private int maxHistoryDays;

    public UserResponse() {
        //empty
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
    public String getDisplayName() { return displayName; }
    public void setDisplayName(String displayName) { this.displayName = displayName; }
    public String getPlanType() { return planType; }
    public void setPlanType(String planType) { this.planType = planType; }
    public boolean isHasAds() { return hasAds; }
    public void setHasAds(boolean hasAds) { this.hasAds = hasAds; }
    public int getMaxPortfolios() { return maxPortfolios; }
    public void setMaxPortfolios(int maxPortfolios) { this.maxPortfolios = maxPortfolios; }
    public int getMaxDcaPlans() { return maxDcaPlans; }
    public void setMaxDcaPlans(int maxDcaPlans) { this.maxDcaPlans = maxDcaPlans; }
    public int getMaxHistoryDays() { return maxHistoryDays; }
    public void setMaxHistoryDays(int maxHistoryDays) { this.maxHistoryDays = maxHistoryDays; }
}
