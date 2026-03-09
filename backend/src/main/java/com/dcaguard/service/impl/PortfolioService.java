package com.dcaguard.service.impl;

import com.dcaguard.dto.request.AddPositionRequest;
import com.dcaguard.dto.request.CreatePortfolioRequest;
import com.dcaguard.dto.response.PortfolioAnalysisResponse;
import com.dcaguard.dto.response.PortfolioResponse;
import com.dcaguard.dto.response.PositionResponse;
import com.dcaguard.entity.*;
import com.dcaguard.exception.PlanLimitExceededException;
import com.dcaguard.exception.ResourceNotFoundException;
import com.dcaguard.repository.*;
import com.dcaguard.service.market.MarketDataProvider;
import com.dcaguard.service.usecase.RiskScoreCalculator;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class PortfolioService {

    public static final String PORTFOLIO_NOT_FOUND = "Portfolio not found";
    private final PortfolioRepository portfolioRepository;
    private final PortfolioPositionRepository positionRepository;
    private final AssetRepository assetRepository;
    private final UserRepository userRepository;
    private final MarketDataProvider marketDataProvider;
    private final RiskScoreCalculator riskCalculator;

    public PortfolioService(PortfolioRepository portfolioRepository,
                            PortfolioPositionRepository positionRepository,
                            AssetRepository assetRepository,
                            UserRepository userRepository,
                            MarketDataProvider marketDataProvider,
                            RiskScoreCalculator riskCalculator) {
        this.portfolioRepository = portfolioRepository;
        this.positionRepository = positionRepository;
        this.assetRepository = assetRepository;
        this.userRepository = userRepository;
        this.marketDataProvider = marketDataProvider;
        this.riskCalculator = riskCalculator;
    }

    @Transactional
    public PortfolioResponse createPortfolio(Long userId, CreatePortfolioRequest request) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        long count = portfolioRepository.countByUserId(userId);
        if (count >= user.getMaxPortfolios()) {
            throw new PlanLimitExceededException("portfolios", user.getMaxPortfolios());
        }

        Portfolio portfolio = new Portfolio(user, request.getName(), request.getBaseCurrency());
        portfolio = portfolioRepository.save(portfolio);
        return toResponse(portfolio);
    }

    public List<PortfolioResponse> getUserPortfolios(Long userId) {
        return portfolioRepository.findByUserId(userId).stream()
                .map(this::toResponse)
                .toList();
    }

    public PortfolioResponse getPortfolio(Long userId, Long portfolioId) {
        Portfolio portfolio = portfolioRepository.findByIdAndUserId(portfolioId, userId)
                .orElseThrow(() -> new ResourceNotFoundException(PORTFOLIO_NOT_FOUND));
        return toResponse(portfolio);
    }

    @Transactional
    public PositionResponse addPosition(Long userId, Long portfolioId, AddPositionRequest request) {
        Portfolio portfolio = portfolioRepository.findByIdAndUserId(portfolioId, userId)
                .orElseThrow(() -> new ResourceNotFoundException(PORTFOLIO_NOT_FOUND));

        Asset asset = assetRepository.findBySymbol(request.getSymbol().toUpperCase())
                .orElseThrow(() -> new ResourceNotFoundException("Asset not found: " + request.getSymbol()));

        Optional<PortfolioPosition> existing = positionRepository
                .findByPortfolioIdAndAssetId(portfolioId, asset.getId());

        PortfolioPosition position;
        if (existing.isPresent()) {
            position = existing.get();
            position.setQuantity(request.getQuantity());
            if (request.getAvgBuyPrice() != null) {
                position.setAvgBuyPrice(request.getAvgBuyPrice());
            }
            if (request.getNotes() != null) {
                position.setNotes(request.getNotes());
            }
        } else {
            position = new PortfolioPosition(portfolio, asset, request.getQuantity(), request.getAvgBuyPrice());
            position.setNotes(request.getNotes());
        }

        position = positionRepository.save(position);
        return toPositionResponse(position, portfolio.getBaseCurrency(), BigDecimal.ZERO);
    }

    @Transactional
    public void deletePosition(Long userId, Long portfolioId, Long positionId) {
        Portfolio portfolio = portfolioRepository.findByIdAndUserId(portfolioId, userId)
                .orElseThrow(() -> new ResourceNotFoundException(PORTFOLIO_NOT_FOUND));

        PortfolioPosition position = positionRepository.findByIdAndPortfolioId(positionId, portfolioId)
                .orElseThrow(() -> new ResourceNotFoundException("Position not found"));

        positionRepository.delete(position);
    }

    public PortfolioAnalysisResponse analyzePortfolio(Long userId, Long portfolioId) {
        Portfolio portfolio = portfolioRepository.findByIdAndUserId(portfolioId, userId)
                .orElseThrow(() -> new ResourceNotFoundException(PORTFOLIO_NOT_FOUND));

        List<PortfolioPosition> positions = portfolio.getPositions();
        if (positions.isEmpty()) {
            PortfolioAnalysisResponse empty = new PortfolioAnalysisResponse();
            empty.setTotalValue(BigDecimal.ZERO);
            empty.setTotalPnl(BigDecimal.ZERO);
            empty.setTotalPnlPercent(BigDecimal.ZERO);
            empty.setRiskScore(0);
            empty.setRiskLevel("N/A");
            empty.setPositions(List.of());
            empty.setAllocationByAsset(List.of());
            empty.setAllocationByCategory(List.of());
            empty.setInsights(List.of("Add positions to your portfolio to see analysis."));
            return empty;
        }

        // Fetch current prices
        String currency = portfolio.getBaseCurrency().toLowerCase();
        List<String> providerIds = positions.stream()
                .map(p -> p.getAsset().getProviderId())
                .toList();
        Map<String, BigDecimal> prices = marketDataProvider.getMultiplePrices(providerIds, currency);

        // Calculate position values
        BigDecimal totalValue = BigDecimal.ZERO;
        BigDecimal totalCost = BigDecimal.ZERO;
        List<PositionResponse> posResponses = new ArrayList<>();

        for (PortfolioPosition pos : positions) {
            BigDecimal price = prices.getOrDefault(pos.getAsset().getProviderId(), BigDecimal.ZERO);
            BigDecimal value = pos.getQuantity().multiply(price).setScale(2, RoundingMode.HALF_UP);
            totalValue = totalValue.add(value);

            if (pos.getAvgBuyPrice() != null) {
                totalCost = totalCost.add(pos.getQuantity().multiply(pos.getAvgBuyPrice())
                        .setScale(2, RoundingMode.HALF_UP));
            }

            posResponses.add(toPositionResponse(pos, currency, price));
        }

        // Calculate allocations
        BigDecimal finalTotalValue = totalValue;
        List<BigDecimal> allocations = new ArrayList<>();
        Map<String, BigDecimal> categoryAllocations = new HashMap<>();

        for (int i = 0; i < positions.size(); i++) {
            BigDecimal price = prices.getOrDefault(positions.get(i).getAsset().getProviderId(), BigDecimal.ZERO);
            BigDecimal value = positions.get(i).getQuantity().multiply(price);
            BigDecimal pct = finalTotalValue.compareTo(BigDecimal.ZERO) > 0
                    ? value.divide(finalTotalValue, 4, RoundingMode.HALF_UP).multiply(new BigDecimal("100"))
                    : BigDecimal.ZERO;
            allocations.add(pct);
            posResponses.get(i).setAllocationPercent(pct.setScale(2, RoundingMode.HALF_UP));

            String cat = positions.get(i).getAsset().getCategory();
            categoryAllocations.merge(cat, pct, BigDecimal::add);
        }

        // Build allocation breakdowns
        List<PortfolioAnalysisResponse.AllocationItem> byAsset = new ArrayList<>();
        for (int i = 0; i < positions.size(); i++) {
            BigDecimal price = prices.getOrDefault(positions.get(i).getAsset().getProviderId(), BigDecimal.ZERO);
            BigDecimal value = positions.get(i).getQuantity().multiply(price).setScale(2, RoundingMode.HALF_UP);
            byAsset.add(new PortfolioAnalysisResponse.AllocationItem(
                    positions.get(i).getAsset().getSymbol(), value, allocations.get(i)));
        }

        List<PortfolioAnalysisResponse.AllocationItem> byCategory = categoryAllocations.entrySet().stream()
                .map(e -> new PortfolioAnalysisResponse.AllocationItem(e.getKey(), null, e.getValue()))
                .sorted(Comparator.comparing(PortfolioAnalysisResponse.AllocationItem::getPercent).reversed())
                .toList();

        // Calculate risk scores
        int concentrationScore = riskCalculator.calculateConcentrationScore(allocations);
        int categoryScore = riskCalculator.calculateCategoryRiskScore(categoryAllocations);

        // Fetch price history for volatility/drawdown (weighted by allocation)
        int volatilityScore = 0;
        int drawdownScore = 0;
        for (int i = 0; i < positions.size(); i++) {
            String pid = positions.get(i).getAsset().getProviderId();
            List<BigDecimal> history = marketDataProvider.getPriceHistory(pid, currency, 90);
            if (!history.isEmpty()) {
                double weight = allocations.get(i).doubleValue() / 100.0;
                volatilityScore += (int) (riskCalculator.calculateVolatilityScore(history.subList(
                        Math.max(0, history.size() - 30), history.size())) * weight);
                drawdownScore += (int) (riskCalculator.calculateDrawdownScore(history) * weight);
            }
        }

        int compositeScore = riskCalculator.computeCompositeScore(
                concentrationScore, categoryScore, volatilityScore, drawdownScore);

        List<String> insights = riskCalculator.generateInsights(
                concentrationScore, categoryScore, volatilityScore, drawdownScore,
                categoryAllocations, allocations);

        // Build response
        PortfolioAnalysisResponse response = new PortfolioAnalysisResponse();
        response.setTotalValue(totalValue);
        BigDecimal pnl = totalCost.compareTo(BigDecimal.ZERO) > 0
                ? totalValue.subtract(totalCost) : BigDecimal.ZERO;
        response.setTotalPnl(pnl);
        response.setTotalPnlPercent(totalCost.compareTo(BigDecimal.ZERO) > 0
                ? pnl.divide(totalCost, 4, RoundingMode.HALF_UP).multiply(new BigDecimal("100"))
                : BigDecimal.ZERO);
        response.setRiskScore(compositeScore);
        response.setRiskLevel(riskCalculator.getRiskLevel(compositeScore));
        response.setPositions(posResponses);
        response.setAllocationByAsset(byAsset);
        response.setAllocationByCategory(byCategory);

        PortfolioAnalysisResponse.RiskBreakdown breakdown = new PortfolioAnalysisResponse.RiskBreakdown();
        breakdown.setConcentrationScore(concentrationScore);
        breakdown.setCategoryRiskScore(categoryScore);
        breakdown.setVolatilityScore(volatilityScore);
        breakdown.setDrawdownScore(drawdownScore);
        response.setRiskBreakdown(breakdown);
        response.setInsights(insights);

        return response;
    }

    private PortfolioResponse toResponse(Portfolio p) {
        return new PortfolioResponse(p.getId(), p.getName(), p.getBaseCurrency(),
                p.getPositions().size(), p.getCreatedAt());
    }

    private PositionResponse toPositionResponse(PortfolioPosition pos, String currency, BigDecimal currentPrice) {
        if (currentPrice.compareTo(BigDecimal.ZERO) == 0) {
            currentPrice = marketDataProvider.getCurrentPrice(pos.getAsset().getProviderId(), currency);
        }
        PositionResponse r = new PositionResponse();
        r.setId(pos.getId());
        r.setSymbol(pos.getAsset().getSymbol());
        r.setAssetName(pos.getAsset().getName());
        r.setCategory(pos.getAsset().getCategory());
        r.setQuantity(pos.getQuantity());
        r.setAvgBuyPrice(pos.getAvgBuyPrice());
        r.setCurrentPrice(currentPrice);
        r.setCurrentValue(pos.getQuantity().multiply(currentPrice).setScale(2, RoundingMode.HALF_UP));
        r.setNotes(pos.getNotes());

        if (pos.getAvgBuyPrice() != null && pos.getAvgBuyPrice().compareTo(BigDecimal.ZERO) > 0) {
            BigDecimal cost = pos.getQuantity().multiply(pos.getAvgBuyPrice());
            BigDecimal value = pos.getQuantity().multiply(currentPrice);
            BigDecimal pnl = value.subtract(cost).setScale(2, RoundingMode.HALF_UP);
            r.setPnl(pnl);
            r.setPnlPercent(cost.compareTo(BigDecimal.ZERO) > 0
                    ? pnl.divide(cost, 4, RoundingMode.HALF_UP).multiply(new BigDecimal("100")).setScale(2, RoundingMode.HALF_UP)
                    : BigDecimal.ZERO);
        }

        return r;
    }
}
