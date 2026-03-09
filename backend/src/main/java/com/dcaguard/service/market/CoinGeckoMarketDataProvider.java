package com.dcaguard.service.market;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Component
@ConditionalOnProperty(name = "market.provider", havingValue = "coingecko", matchIfMissing = true)
public class CoinGeckoMarketDataProvider implements MarketDataProvider {

    private static final Logger log = LoggerFactory.getLogger(CoinGeckoMarketDataProvider.class);

    private final String baseUrl;
    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper;

    private final Map<String, CachedPrice> priceCache = new ConcurrentHashMap<>();
    private static final long CACHE_TTL_MS = 60_000;

    public CoinGeckoMarketDataProvider(
            @Value("${market.coingecko.base-url:https://api.coingecko.com/api/v3}") String baseUrl) {
        this.baseUrl = baseUrl;
        this.restTemplate = new RestTemplate();
        this.objectMapper = new ObjectMapper();
    }

    @Override
    public List<AssetSnapshot> getTopAssets(int limit, String currency) {
        try {
            int safeLimit = Math.clamp(limit, 1, 250);
            String url = baseUrl + "/coins/markets?vs_currency=" + currency.toLowerCase()
                    + "&order=market_cap_desc&per_page=" + safeLimit + "&page=1&sparkline=false";

            String response = restTemplate.getForObject(url, String.class);
            JsonNode root = objectMapper.readTree(response);

            List<AssetSnapshot> result = new ArrayList<>();
            for (JsonNode node : root) {
                String id = node.path("id").asText();
                String symbol = node.path("symbol").asText("").toUpperCase();
                String name = node.path("name").asText("");
                String image = node.path("image").asText(null);
                Integer rank = node.path("market_cap_rank").isMissingNode() ? null : node.path("market_cap_rank").asInt();

                if (!id.isBlank() && !symbol.isBlank()) {
                    result.add(new AssetSnapshot(id, symbol, name, image, rank));
                }
            }
            return result;
        } catch (Exception e) {
            log.error("Failed to fetch top assets: {}", e.getMessage());
            return List.of();
        }
    }

    @Override
    public BigDecimal getCurrentPrice(String providerId, String currency) {
        String cacheKey = providerId + ":" + currency.toLowerCase();
        CachedPrice cached = priceCache.get(cacheKey);
        if (cached != null && !cached.isExpired()) {
            return cached.price;
        }

        try {
            String url = baseUrl + "/simple/price?ids=" + providerId +
                    "&vs_currencies=" + currency.toLowerCase();
            String response = restTemplate.getForObject(url, String.class);
            JsonNode root = objectMapper.readTree(response);
            BigDecimal price = root.path(providerId).path(currency.toLowerCase()).decimalValue();
            priceCache.put(cacheKey, new CachedPrice(price));
            return price;
        } catch (Exception e) {
            log.error("Failed to fetch price for {}: {}", providerId, e.getMessage());
            return cached != null ? cached.price : BigDecimal.ZERO;
        }
    }

    @Override
    public Map<String, BigDecimal> getMultiplePrices(List<String> providerIds, String currency) {
        Map<String, BigDecimal> results = new HashMap<>();
        List<String> toFetch = new ArrayList<>();

        for (String id : providerIds) {
            String cacheKey = id + ":" + currency.toLowerCase();
            CachedPrice cached = priceCache.get(cacheKey);
            if (cached != null && !cached.isExpired()) {
                results.put(id, cached.price);
            } else {
                toFetch.add(id);
            }
        }

        if (!toFetch.isEmpty()) {
            try {
                String ids = String.join(",", toFetch);
                String url = baseUrl + "/simple/price?ids=" + ids +
                        "&vs_currencies=" + currency.toLowerCase();
                String response = restTemplate.getForObject(url, String.class);
                JsonNode root = objectMapper.readTree(response);

                for (String id : toFetch) {
                    BigDecimal price = root.path(id).path(currency.toLowerCase()).decimalValue();
                    results.put(id, price);
                    priceCache.put(id + ":" + currency.toLowerCase(), new CachedPrice(price));
                }
            } catch (Exception e) {
                log.error("Failed to fetch multiple prices: {}", e.getMessage());
                for (String id : toFetch) {
                    results.putIfAbsent(id, BigDecimal.ZERO);
                }
            }
        }

        return results;
    }

    @Override
    public List<BigDecimal> getPriceHistory(String providerId, String currency, int days) {
        try {
            String url = baseUrl + "/coins/" + providerId +
                    "/market_chart?vs_currency=" + currency.toLowerCase() +
                    "&days=" + days + "&interval=daily";
            String response = restTemplate.getForObject(url, String.class);
            JsonNode root = objectMapper.readTree(response);
            JsonNode prices = root.path("prices");

            List<BigDecimal> result = new ArrayList<>();
            for (JsonNode point : prices) {
                result.add(BigDecimal.valueOf(point.get(1).asDouble()).setScale(8, RoundingMode.HALF_UP));
            }
            return result;
        } catch (Exception e) {
            log.error("Failed to fetch price history for {}: {}", providerId, e.getMessage());
            return List.of();
        }
    }

    @Override
    public List<PricePoint> getPriceHistoryWithDates(String providerId, String currency, int days) {
        try {
            String url = baseUrl + "/coins/" + providerId +
                    "/market_chart?vs_currency=" + currency.toLowerCase() +
                    "&days=" + days + "&interval=daily";
            String response = restTemplate.getForObject(url, String.class);
            JsonNode root = objectMapper.readTree(response);
            JsonNode prices = root.path("prices");

            List<PricePoint> result = new ArrayList<>();
            DateTimeFormatter fmt = DateTimeFormatter.ISO_LOCAL_DATE;
            for (JsonNode point : prices) {
                long timestamp = point.get(0).asLong();
                LocalDate date = Instant.ofEpochMilli(timestamp)
                        .atZone(ZoneId.of("UTC")).toLocalDate();
                BigDecimal price = BigDecimal.valueOf(point.get(1).asDouble())
                        .setScale(8, RoundingMode.HALF_UP);
                result.add(new PricePoint(date.format(fmt), price));
            }
            return result;
        } catch (Exception e) {
            log.error("Failed to fetch price history with dates for {}: {}", providerId, e.getMessage());
            return List.of();
        }
    }

    private static class CachedPrice {
        final BigDecimal price;
        final long timestamp;

        CachedPrice(BigDecimal price) {
            this.price = price;
            this.timestamp = System.currentTimeMillis();
        }

        boolean isExpired() {
            return System.currentTimeMillis() - timestamp > CACHE_TTL_MS;
        }
    }
}
