package com.dcaguard.service.market;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;

@Component
@ConditionalOnProperty(name = "market.provider", havingValue = "mock")
public class MockMarketDataProvider implements MarketDataProvider {

    private static final Map<String, BigDecimal> MOCK_PRICES = Map.of(
            "bitcoin", new BigDecimal("95000.00"),
            "ethereum", new BigDecimal("3500.00"),
            "solana", new BigDecimal("180.00"),
            "dogecoin", new BigDecimal("0.35"),
            "chainlink", new BigDecimal("22.00")
    );

    @Override
    public List<AssetSnapshot> getTopAssets(int limit, String currency) {
        List<AssetSnapshot> list = new ArrayList<>();

        int i = 1;
        for (var entry : MOCK_PRICES.entrySet()) {
            if (i > limit) break;

            list.add(new AssetSnapshot(
                    entry.getKey(), // providerId
                    entry.getKey().toUpperCase(),
                    entry.getKey().substring(0, 1).toUpperCase() + entry.getKey().substring(1),
                    null,
                    i
            ));
            i++;
        }
        return list;
    }

    @Override
    public BigDecimal getCurrentPrice(String providerId, String currency) {
        return MOCK_PRICES.getOrDefault(providerId, new BigDecimal("100.00"));
    }

    @Override
    public Map<String, BigDecimal> getMultiplePrices(List<String> providerIds, String currency) {
        Map<String, BigDecimal> result = new HashMap<>();
        for (String id : providerIds) {
            result.put(id, getCurrentPrice(id, currency));
        }
        return result;
    }

    @Override
    public List<BigDecimal> getPriceHistory(String providerId, String currency, int days) {
        BigDecimal currentPrice = getCurrentPrice(providerId, currency);
        List<BigDecimal> history = new ArrayList<>();
        Random random = new Random(providerId.hashCode());
        for (int i = days; i >= 0; i--) {
            double factor = 0.8 + (random.nextDouble() * 0.4);
            history.add(currentPrice.multiply(BigDecimal.valueOf(factor)).setScale(8, RoundingMode.HALF_UP));
        }
        return history;
    }

    @Override
    public List<PricePoint> getPriceHistoryWithDates(String providerId, String currency, int days) {
        List<BigDecimal> prices = getPriceHistory(providerId, currency, days);
        List<PricePoint> result = new ArrayList<>();
        LocalDate startDate = LocalDate.now().minusDays(days);
        DateTimeFormatter fmt = DateTimeFormatter.ISO_LOCAL_DATE;
        for (int i = 0; i < prices.size(); i++) {
            result.add(new PricePoint(startDate.plusDays(i).format(fmt), prices.get(i)));
        }
        return result;
    }
}
