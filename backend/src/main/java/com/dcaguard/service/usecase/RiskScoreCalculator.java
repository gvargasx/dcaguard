package com.dcaguard.service.usecase;

import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.*;

@Component
public class RiskScoreCalculator {

    /**
     * Calculate the concentration risk score (0-100).
     * Higher concentration = higher risk.
     */
    public int calculateConcentrationScore(List<BigDecimal> allocationPercents) {
        if (allocationPercents == null || allocationPercents.isEmpty()) return 0;
        if (allocationPercents.size() == 1) return 100;

        List<BigDecimal> sorted = allocationPercents.stream()
                .sorted(Comparator.reverseOrder())
                .toList();

        BigDecimal top1 = sorted.getFirst();
        BigDecimal top3Sum = sorted.stream().limit(3)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        int score = 0;

        // Top 1 asset contribution (max 50 points)
        if (top1.compareTo(new BigDecimal("80")) >= 0) score += 50;
        else if (top1.compareTo(new BigDecimal("60")) >= 0) score += 40;
        else if (top1.compareTo(new BigDecimal("40")) >= 0) score += 25;
        else if (top1.compareTo(new BigDecimal("25")) >= 0) score += 15;
        else score += 5;

        // Top 3 assets contribution (max 50 points)
        if (top3Sum.compareTo(new BigDecimal("95")) >= 0) score += 50;
        else if (top3Sum.compareTo(new BigDecimal("85")) >= 0) score += 35;
        else if (top3Sum.compareTo(new BigDecimal("70")) >= 0) score += 20;
        else score += 10;

        return Math.min(score, 100);
    }

    /**
     * Calculate category risk score (0-100).
     * High exposure to meme/microcap = higher risk.
     */
    public int calculateCategoryRiskScore(Map<String, BigDecimal> categoryAllocations) {
        if (categoryAllocations == null || categoryAllocations.isEmpty()) return 0;

        Map<String, Integer> riskWeights = Map.of(
                "MEME", 100,
                "MICROCAP", 85,
                "DEFI", 50,
                "LAYER2", 40,
                "PAYMENT", 30,
                "LAYER1", 20,
                "STABLECOIN", 5,
                "GENERAL", 35
        );

        BigDecimal weightedSum = BigDecimal.ZERO;
        for (var entry : categoryAllocations.entrySet()) {
            int weight = riskWeights.getOrDefault(entry.getKey().toUpperCase(), 35);
            weightedSum = weightedSum.add(
                    entry.getValue().multiply(BigDecimal.valueOf(weight))
                            .divide(BigDecimal.valueOf(100), 4, RoundingMode.HALF_UP)
            );
        }

        return Math.min(weightedSum.intValue(), 100);
    }

    /**
     * Calculate volatility score (0-100) from daily returns.
     * Standard deviation of 30-day daily returns.
     */
    public int calculateVolatilityScore(List<BigDecimal> dailyPrices) {
        if (dailyPrices == null || dailyPrices.size() < 2) return 0;

        List<BigDecimal> returns = new ArrayList<>();
        for (int i = 1; i < dailyPrices.size(); i++) {
            if (dailyPrices.get(i - 1).compareTo(BigDecimal.ZERO) > 0) {
                BigDecimal ret = dailyPrices.get(i).subtract(dailyPrices.get(i - 1))
                        .divide(dailyPrices.get(i - 1), 8, RoundingMode.HALF_UP);
                returns.add(ret);
            }
        }

        if (returns.isEmpty()) return 0;

        BigDecimal mean = returns.stream()
                .reduce(BigDecimal.ZERO, BigDecimal::add)
                .divide(BigDecimal.valueOf(returns.size()), 8, RoundingMode.HALF_UP);

        BigDecimal variance = returns.stream()
                .map(r -> r.subtract(mean).pow(2))
                .reduce(BigDecimal.ZERO, BigDecimal::add)
                .divide(BigDecimal.valueOf(returns.size()), 8, RoundingMode.HALF_UP);

        double stdDev = Math.sqrt(variance.doubleValue());
        double annualizedVol = stdDev * Math.sqrt(365);

        // Map to 0-100: 0-30% vol -> 0, 30-100% vol -> 0-50, >100% vol -> 50-100
        if (annualizedVol < 0.3) return (int) (annualizedVol / 0.3 * 30);
        if (annualizedVol < 1.0) return (int) (30 + (annualizedVol - 0.3) / 0.7 * 40);
        return Math.min((int) (70 + (annualizedVol - 1.0) * 30), 100);
    }

    /**
     * Calculate max drawdown score (0-100) from price series.
     */
    public int calculateDrawdownScore(List<BigDecimal> prices) {
        if (prices == null || prices.size() < 2) return 0;

        BigDecimal peak = prices.getFirst();
        BigDecimal maxDrawdown = BigDecimal.ZERO;

        for (BigDecimal price : prices) {
            if (price.compareTo(peak) > 0) peak = price;
            if (peak.compareTo(BigDecimal.ZERO) > 0) {
                BigDecimal drawdown = peak.subtract(price)
                        .divide(peak, 8, RoundingMode.HALF_UP);
                if (drawdown.compareTo(maxDrawdown) > 0) maxDrawdown = drawdown;
            }
        }

        double dd = maxDrawdown.doubleValue() * 100;
        if (dd < 10) return (int) (dd * 2);
        if (dd < 30) return (int) (20 + (dd - 10));
        if (dd < 50) return (int) (40 + (dd - 30) * 1.5);
        return Math.min((int) (70 + (dd - 50)), 100);
    }

    /**
     * Compute composite risk score from components.
     */
    public int computeCompositeScore(int concentration, int categoryRisk, int volatility, int drawdown) {
        double weighted = concentration * 0.30 + categoryRisk * 0.20 + volatility * 0.30 + drawdown * 0.20;
        return Math.min((int) Math.round(weighted), 100);
    }

    /**
     * Get risk level label from score.
     */
    public String getRiskLevel(int score) {
        if (score <= 20) return "Very Low";
        if (score <= 40) return "Low";
        if (score <= 60) return "Moderate";
        if (score <= 80) return "High";
        return "Very High";
    }

    /**
     * Generate educational insights based on analysis.
     */
    public List<String> generateInsights(int concentrationScore, int categoryScore,
                                          int volatilityScore, int drawdownScore,
                                          Map<String, BigDecimal> categoryAllocations,
                                          List<BigDecimal> allocations) {
        List<String> insights = new ArrayList<>();

        if (concentrationScore >= 60 && allocations != null && !allocations.isEmpty()) {
            BigDecimal top = allocations.stream().max(Comparator.naturalOrder()).orElse(BigDecimal.ZERO);
            insights.add(String.format(
                    "High concentration detected: your largest position represents %.0f%% of the portfolio. " +
                    "Diversification across multiple assets can help reduce the impact of a single asset's decline.",
                    top.doubleValue()));
        }

        if (categoryScore >= 50) {
            BigDecimal meme = categoryAllocations.getOrDefault("MEME", BigDecimal.ZERO);
            if (meme.compareTo(new BigDecimal("20")) > 0) {
                insights.add(String.format(
                        "Elevated meme coin exposure at %.0f%%. Meme coins tend to have significantly higher " +
                        "volatility and risk of permanent loss compared to established cryptocurrencies.", meme.doubleValue()));
            }
        }

        if (volatilityScore >= 50) {
            insights.add("Your portfolio has shown high volatility over the last 30 days. " +
                    "Consider that price swings of 10%+ in a single day are common in this composition.");
        }

        if (drawdownScore >= 40) {
            insights.add("Significant drawdown observed in the last 90 days. " +
                    "This means the portfolio dropped substantially from its peak, which is important to factor " +
                    "into your risk tolerance assessment.");
        }

        if (allocations != null && allocations.size() < 3) {
            insights.add("Your portfolio has fewer than 3 assets. " +
                    "Adding more positions across different categories could help spread risk.");
        }

        if (insights.isEmpty()) {
            insights.add("Your portfolio appears relatively balanced. Continue monitoring as market conditions change.");
        }

        return insights;
    }
}
