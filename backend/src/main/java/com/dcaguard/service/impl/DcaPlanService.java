package com.dcaguard.service.impl;

import com.dcaguard.dto.request.AddExecutionRequest;
import com.dcaguard.dto.request.CreateDcaPlanRequest;
import com.dcaguard.dto.response.AlertResponse;
import com.dcaguard.dto.response.DcaPlanResponse;
import com.dcaguard.dto.response.DcaSummaryResponse;
import com.dcaguard.entity.*;
import com.dcaguard.exception.PlanLimitExceededException;
import com.dcaguard.exception.ResourceNotFoundException;
import com.dcaguard.repository.*;
import com.dcaguard.service.market.MarketDataProvider;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.*;

@Service
public class DcaPlanService {

    public static final String DCA_PLAN_NOT_FOUND = "DCA Plan not found";
    private final DcaPlanRepository planRepository;
    private final DcaExecutionRepository executionRepository;
    private final AssetRepository assetRepository;
    private final UserRepository userRepository;
    private final MarketDataProvider marketDataProvider;

    public DcaPlanService(DcaPlanRepository planRepository, DcaExecutionRepository executionRepository,
                          AssetRepository assetRepository, UserRepository userRepository,
                          MarketDataProvider marketDataProvider) {
        this.planRepository = planRepository;
        this.executionRepository = executionRepository;
        this.assetRepository = assetRepository;
        this.userRepository = userRepository;
        this.marketDataProvider = marketDataProvider;
    }

    @Transactional
    public DcaPlanResponse createPlan(Long userId, CreateDcaPlanRequest request) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        long count = planRepository.countByUserId(userId);
        if (count >= user.getMaxDcaPlans()) {
            throw new PlanLimitExceededException("DCA plans", user.getMaxDcaPlans());
        }

        Asset asset = assetRepository.findBySymbol(request.getSymbol().toUpperCase())
                .orElseThrow(() -> new ResourceNotFoundException("Asset not found: " + request.getSymbol()));

        DcaPlan plan = new DcaPlan();
        plan.setUser(user);
        plan.setAsset(asset);
        plan.setAmount(request.getAmount());
        plan.setFrequency(Frequency.valueOf(request.getFrequency()));
        plan.setBaseCurrency(request.getBaseCurrency());
        plan.setStartDate(request.getStartDate());
        plan.setEndDate(request.getEndDate());
        plan.setActive(true);

        plan = planRepository.save(plan);
        return toResponse(plan);
    }

    public List<DcaPlanResponse> getUserPlans(Long userId) {
        return planRepository.findByUserId(userId).stream()
                .map(this::toResponse)
                .toList();
    }

    public DcaPlanResponse getPlan(Long userId, Long planId) {
        DcaPlan plan = planRepository.findByIdAndUserId(planId, userId)
                .orElseThrow(() -> new ResourceNotFoundException(DCA_PLAN_NOT_FOUND));
        return toResponse(plan);
    }

    @Transactional
    public DcaSummaryResponse.ExecutionResponse addExecution(Long userId, Long planId, AddExecutionRequest request) {
        DcaPlan plan = planRepository.findByIdAndUserId(planId, userId)
                .orElseThrow(() -> new ResourceNotFoundException(DCA_PLAN_NOT_FOUND));

        BigDecimal quantity = request.getAmountPaid()
                .divide(request.getPriceAtExec(), 12, RoundingMode.HALF_UP);

        DcaExecution execution = new DcaExecution(plan, request.getExecutionDate(),
                request.getAmountPaid(), request.getPriceAtExec(), quantity, false);
        execution = executionRepository.save(execution);

        DcaSummaryResponse.ExecutionResponse resp = new DcaSummaryResponse.ExecutionResponse();
        resp.setId(execution.getId());
        resp.setDate(execution.getExecutionDate().toString());
        resp.setAmountPaid(execution.getAmountPaid());
        resp.setPriceAtExec(execution.getPriceAtExec());
        resp.setQuantity(execution.getQuantity());
        resp.setSimulated(execution.isSimulated());
        return resp;
    }

    public DcaSummaryResponse getSummary(Long userId, Long planId) {
        DcaPlan plan = planRepository.findByIdAndUserId(planId, userId)
                .orElseThrow(() -> new ResourceNotFoundException(DCA_PLAN_NOT_FOUND));

        User user = plan.getUser();
        int maxDays = user.getMaxHistoryDays();
        String currency = plan.getBaseCurrency().toLowerCase();
        String providerId = plan.getAsset().getProviderId();

        // Get executions (real ones)
        List<DcaExecution> realExecs = executionRepository.findByPlanIdOrderByExecutionDateAsc(plan.getId());

        // Simulate DCA based on historical prices
        List<MarketDataProvider.PricePoint> priceHistory = marketDataProvider
                .getPriceHistoryWithDates(providerId, currency, maxDays);

        // Generate simulated execution dates
        List<LocalDate> scheduledDates = generateScheduledDates(
                plan.getStartDate(),
                plan.getEndDate() != null ? plan.getEndDate() : LocalDate.now(),
                plan.getFrequency());

        // Build price lookup
        Map<String, BigDecimal> priceLookup = new HashMap<>();
        for (MarketDataProvider.PricePoint pp : priceHistory) {
            priceLookup.put(pp.date(), pp.price());
        }

        // Merge real + simulated executions
        Set<String> realDates = new HashSet<>();
        for (DcaExecution re : realExecs) {
            realDates.add(re.getExecutionDate().toString());
        }

        List<DcaExecution> allExecs = new ArrayList<>(realExecs);
        for (LocalDate date : scheduledDates) {
            String dateStr = date.toString();
            if (!realDates.contains(dateStr) && priceLookup.containsKey(dateStr)) {
                BigDecimal price = priceLookup.get(dateStr);
                if (price.compareTo(BigDecimal.ZERO) > 0) {
                    BigDecimal qty = plan.getAmount().divide(price, 12, RoundingMode.HALF_UP);
                    allExecs.add(new DcaExecution(plan, date, plan.getAmount(), price, qty, true));
                }
            }
        }

        allExecs.sort(Comparator.comparing(DcaExecution::getExecutionDate));

        // Calculate summary metrics
        BigDecimal totalInvested = BigDecimal.ZERO;
        BigDecimal totalQuantity = BigDecimal.ZERO;
        List<DcaSummaryResponse.DcaChartPoint> chartData = new ArrayList<>();
        List<DcaSummaryResponse.ExecutionResponse> execResponses = new ArrayList<>();

        for (DcaExecution exec : allExecs) {
            totalInvested = totalInvested.add(exec.getAmountPaid());
            totalQuantity = totalQuantity.add(exec.getQuantity());

            BigDecimal avgPrice = totalQuantity.compareTo(BigDecimal.ZERO) > 0
                    ? totalInvested.divide(totalQuantity, 8, RoundingMode.HALF_UP)
                    : BigDecimal.ZERO;

            BigDecimal marketPrice = exec.getPriceAtExec();
            BigDecimal portfolioValue = totalQuantity.multiply(marketPrice).setScale(2, RoundingMode.HALF_UP);

            chartData.add(new DcaSummaryResponse.DcaChartPoint(
                    exec.getExecutionDate().toString(),
                    totalInvested.setScale(2, RoundingMode.HALF_UP),
                    portfolioValue,
                    avgPrice.setScale(2, RoundingMode.HALF_UP),
                    marketPrice.setScale(2, RoundingMode.HALF_UP)
            ));

            DcaSummaryResponse.ExecutionResponse er = new DcaSummaryResponse.ExecutionResponse();
            er.setId(exec.getId());
            er.setDate(exec.getExecutionDate().toString());
            er.setAmountPaid(exec.getAmountPaid());
            er.setPriceAtExec(exec.getPriceAtExec());
            er.setQuantity(exec.getQuantity());
            er.setSimulated(exec.isSimulated());
            execResponses.add(er);
        }

        BigDecimal currentPrice = marketDataProvider.getCurrentPrice(providerId, currency);
        BigDecimal avgPrice = totalQuantity.compareTo(BigDecimal.ZERO) > 0
                ? totalInvested.divide(totalQuantity, 8, RoundingMode.HALF_UP)
                : BigDecimal.ZERO;
        BigDecimal currentValue = totalQuantity.multiply(currentPrice).setScale(2, RoundingMode.HALF_UP);
        BigDecimal pnl = currentValue.subtract(totalInvested);
        BigDecimal pnlPercent = totalInvested.compareTo(BigDecimal.ZERO) > 0
                ? pnl.divide(totalInvested, 4, RoundingMode.HALF_UP).multiply(new BigDecimal("100"))
                : BigDecimal.ZERO;

        DcaSummaryResponse summary = new DcaSummaryResponse();
        summary.setPlanId(planId);
        summary.setSymbol(plan.getAsset().getSymbol());
        summary.setTotalInvested(totalInvested.setScale(2, RoundingMode.HALF_UP));
        summary.setTotalQuantity(totalQuantity);
        summary.setAveragePrice(avgPrice.setScale(2, RoundingMode.HALF_UP));
        summary.setCurrentPrice(currentPrice);
        summary.setCurrentValue(currentValue);
        summary.setPnl(pnl.setScale(2, RoundingMode.HALF_UP));
        summary.setPnlPercent(pnlPercent.setScale(2, RoundingMode.HALF_UP));
        summary.setTotalExecutions(allExecs.size());
        summary.setChartData(chartData);
        summary.setExecutions(execResponses);

        return summary;
    }

    public List<AlertResponse> getAlerts(Long userId, Long planId) {
        DcaPlan plan = planRepository.findByIdAndUserId(planId, userId)
                .orElseThrow(() -> new ResourceNotFoundException(DCA_PLAN_NOT_FOUND));

        String currency = plan.getBaseCurrency().toLowerCase();
        String providerId = plan.getAsset().getProviderId();
        BigDecimal currentPrice = marketDataProvider.getCurrentPrice(providerId, currency);

        List<BigDecimal> priceHistory = marketDataProvider.getPriceHistory(providerId, currency, 7);
        List<AlertResponse> alerts = new ArrayList<>();

        // Dip Alert: price dropped X% in last 7 days
        if (priceHistory.size() >= 2) {
            BigDecimal highPrice = priceHistory.stream().max(Comparator.naturalOrder()).orElse(currentPrice);
            if (highPrice.compareTo(BigDecimal.ZERO) > 0) {
                BigDecimal dropPercent = highPrice.subtract(currentPrice)
                        .divide(highPrice, 4, RoundingMode.HALF_UP)
                        .multiply(new BigDecimal("100"));
                if (dropPercent.compareTo(new BigDecimal("5")) >= 0) {
                    String severity = dropPercent.compareTo(new BigDecimal("15")) >= 0 ? "HIGH" :
                            dropPercent.compareTo(new BigDecimal("10")) >= 0 ? "MEDIUM" : "LOW";
                    alerts.add(new AlertResponse(
                            "DIP_ALERT", severity,
                            String.format("%s dropped %.1f%% from its 7-day high. Could be a good DCA opportunity.",
                                    plan.getAsset().getSymbol(), dropPercent.doubleValue()),
                            currentPrice, highPrice, dropPercent.negate()
                    ));
                }
            }
        }

        // Below Average Alert
        List<DcaExecution> executions = executionRepository.findByPlanIdOrderByExecutionDateAsc(plan.getId());
        BigDecimal totalPaid = BigDecimal.ZERO;
        BigDecimal totalQty = BigDecimal.ZERO;
        for (DcaExecution exec : executions) {
            totalPaid = totalPaid.add(exec.getAmountPaid());
            totalQty = totalQty.add(exec.getQuantity());
        }

        if (totalQty.compareTo(BigDecimal.ZERO) > 0) {
            BigDecimal avgPrice = totalPaid.divide(totalQty, 8, RoundingMode.HALF_UP);
            if (currentPrice.compareTo(avgPrice) < 0) {
                BigDecimal belowPercent = avgPrice.subtract(currentPrice)
                        .divide(avgPrice, 4, RoundingMode.HALF_UP)
                        .multiply(new BigDecimal("100"));
                alerts.add(new AlertResponse(
                        "BELOW_AVERAGE", "INFO",
                        String.format("Current %s price is %.1f%% below your DCA average of %s %s. " +
                                      "Buying now would lower your average cost.",
                                plan.getAsset().getSymbol(), belowPercent.doubleValue(),
                                avgPrice.setScale(2, RoundingMode.HALF_UP), plan.getBaseCurrency()),
                        currentPrice, avgPrice, belowPercent.negate()
                ));
            }
        }

        return alerts;
    }

    private List<LocalDate> generateScheduledDates(LocalDate start, LocalDate end, Frequency frequency) {
        List<LocalDate> dates = new ArrayList<>();
        LocalDate current = start;
        while (!current.isAfter(end) && !current.isAfter(LocalDate.now())) {
            dates.add(current);
            current = switch (frequency) {
                case DAILY -> current.plusDays(1);
                case WEEKLY -> current.plusWeeks(1);
                case BIWEEKLY -> current.plusWeeks(2);
                case MONTHLY -> current.plusMonths(1);
            };
        }
        return dates;
    }

    private DcaPlanResponse toResponse(DcaPlan plan) {
        DcaPlanResponse r = new DcaPlanResponse();
        r.setId(plan.getId());
        r.setSymbol(plan.getAsset().getSymbol());
        r.setAssetName(plan.getAsset().getName());
        r.setAmount(plan.getAmount());
        r.setFrequency(plan.getFrequency().name());
        r.setBaseCurrency(plan.getBaseCurrency());
        r.setStartDate(plan.getStartDate());
        r.setEndDate(plan.getEndDate());
        r.setActive(plan.isActive());
        r.setExecutionCount(plan.getExecutions().size());
        r.setCreatedAt(plan.getCreatedAt());
        return r;
    }
}
