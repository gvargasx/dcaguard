package com.dcaguard.service.market;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

public interface MarketDataProvider {

    BigDecimal getCurrentPrice(String providerId, String currency);

    List<BigDecimal> getPriceHistory(String providerId, String currency, int days);

    Map<String, BigDecimal> getMultiplePrices(List<String> providerIds, String currency);

    record PricePoint(String date, BigDecimal price) {
    }

    List<PricePoint> getPriceHistoryWithDates(String providerId, String currency, int days);

    List<AssetSnapshot> getTopAssets(int limit, String currency);

    record AssetSnapshot(
            String providerId,
            String symbol,
            String name,
            String imageUrl,
            Integer marketCapRank
    ) {
    }
}
