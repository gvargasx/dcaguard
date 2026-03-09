package com.dcaguard.service;

import com.dcaguard.service.usecase.RiskScoreCalculator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class RiskScoreCalculatorTest {

    private RiskScoreCalculator calculator;

    @BeforeEach
    void setUp() {
        calculator = new RiskScoreCalculator();
    }

    @Test
    void concentrationScore_singleAsset_returns100() {
        List<BigDecimal> allocations = List.of(new BigDecimal("100"));
        assertEquals(100, calculator.calculateConcentrationScore(allocations));
    }

    @Test
    void concentrationScore_evenlyDistributed_returnsLow() {
        List<BigDecimal> allocations = List.of(
                new BigDecimal("20"), new BigDecimal("20"),
                new BigDecimal("20"), new BigDecimal("20"), new BigDecimal("20"));
        int score = calculator.calculateConcentrationScore(allocations);
        assertTrue(score <= 30, "Expected low concentration score, got: " + score);
    }

    @Test
    void concentrationScore_highConcentration_returnsHigh() {
        List<BigDecimal> allocations = List.of(
                new BigDecimal("85"), new BigDecimal("10"), new BigDecimal("5"));
        int score = calculator.calculateConcentrationScore(allocations);
        assertTrue(score >= 80, "Expected high score, got: " + score);
    }

    @Test
    void concentrationScore_empty_returnsZero() {
        assertEquals(0, calculator.calculateConcentrationScore(List.of()));
    }

    @Test
    void categoryRisk_allMeme_returnsHigh() {
        Map<String, BigDecimal> allocs = Map.of("MEME", new BigDecimal("100"));
        int score = calculator.calculateCategoryRiskScore(allocs);
        assertTrue(score >= 80, "Expected high category risk, got: " + score);
    }

    @Test
    void categoryRisk_allStablecoin_returnsVeryLow() {
        Map<String, BigDecimal> allocs = Map.of("STABLECOIN", new BigDecimal("100"));
        int score = calculator.calculateCategoryRiskScore(allocs);
        assertTrue(score <= 10, "Expected very low category risk, got: " + score);
    }

    @Test
    void categoryRisk_mixed_returnsModerate() {
        Map<String, BigDecimal> allocs = Map.of(
                "LAYER1", new BigDecimal("60"),
                "MEME", new BigDecimal("20"),
                "DEFI", new BigDecimal("20"));
        int score = calculator.calculateCategoryRiskScore(allocs);
        assertTrue(score > 20 && score < 60, "Expected moderate, got: " + score);
    }

    @Test
    void volatilityScore_stablePrices_returnsLow() {
        List<BigDecimal> prices = new java.util.ArrayList<>();
        for (int i = 0; i < 30; i++) {
            prices.add(new BigDecimal("100.00").add(new BigDecimal(String.valueOf(Math.sin(i) * 0.5))));
        }
        int score = calculator.calculateVolatilityScore(prices);
        assertTrue(score <= 30, "Expected low volatility, got: " + score);
    }

    @Test
    void volatilityScore_highVolatility_returnsHigh() {
        List<BigDecimal> prices = List.of(
                new BigDecimal("100"), new BigDecimal("120"), new BigDecimal("80"),
                new BigDecimal("130"), new BigDecimal("70"), new BigDecimal("140"),
                new BigDecimal("60"), new BigDecimal("150"), new BigDecimal("50"),
                new BigDecimal("160"));
        int score = calculator.calculateVolatilityScore(prices);
        assertTrue(score >= 40, "Expected high volatility score, got: " + score);
    }

    @Test
    void volatilityScore_emptyOrSingle_returnsZero() {
        assertEquals(0, calculator.calculateVolatilityScore(List.of()));
        assertEquals(0, calculator.calculateVolatilityScore(List.of(new BigDecimal("100"))));
    }

    @Test
    void drawdownScore_noDrawdown_returnsZero() {
        List<BigDecimal> prices = List.of(
                new BigDecimal("100"), new BigDecimal("110"),
                new BigDecimal("120"), new BigDecimal("130"));
        int score = calculator.calculateDrawdownScore(prices);
        assertEquals(0, score);
    }

    @Test
    void drawdownScore_50percentDrawdown_returnsHigh() {
        List<BigDecimal> prices = List.of(
                new BigDecimal("100"), new BigDecimal("120"),
                new BigDecimal("60"), new BigDecimal("70"));
        int score = calculator.calculateDrawdownScore(prices);
        assertTrue(score >= 40, "Expected high drawdown score, got: " + score);
    }

    @Test
    void compositeScore_allZero_returnsZero() {
        assertEquals(0, calculator.computeCompositeScore(0, 0, 0, 0));
    }

    @Test
    void compositeScore_allHundred_returnsHundred() {
        assertEquals(100, calculator.computeCompositeScore(100, 100, 100, 100));
    }

    @Test
    void riskLevel_mappingsCorrect() {
        assertEquals("Very Low", calculator.getRiskLevel(10));
        assertEquals("Low", calculator.getRiskLevel(30));
        assertEquals("Moderate", calculator.getRiskLevel(50));
        assertEquals("High", calculator.getRiskLevel(70));
        assertEquals("Very High", calculator.getRiskLevel(90));
    }

    @Test
    void generateInsights_highConcentration_producesInsight() {
        List<String> insights = calculator.generateInsights(
                70, 30, 30, 20,
                Map.of("LAYER1", new BigDecimal("100")),
                List.of(new BigDecimal("80"), new BigDecimal("20")));
        assertTrue(insights.stream().anyMatch(i -> i.contains("concentration")),
                "Should mention concentration");
    }

    @Test
    void generateInsights_fewAssets_suggestsDiversification() {
        List<String> insights = calculator.generateInsights(
                30, 30, 30, 20,
                Map.of("LAYER1", new BigDecimal("100")),
                List.of(new BigDecimal("50"), new BigDecimal("50")));
        assertTrue(insights.stream().anyMatch(i -> i.contains("fewer than 3")),
                "Should suggest adding more assets");
    }
}
