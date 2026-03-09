package com.dcaguard.controller;

import com.dcaguard.entity.Asset;
import com.dcaguard.repository.AssetRepository;
import com.dcaguard.service.market.MarketDataProvider;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/market")
public class MarketController {

    private final AssetRepository assetRepository;
    private final MarketDataProvider marketDataProvider;

    public MarketController(AssetRepository assetRepository, MarketDataProvider marketDataProvider) {
        this.assetRepository = assetRepository;
        this.marketDataProvider = marketDataProvider;
    }

    // 1) Assets com limite (evita findAll infinito)
    @GetMapping("/assets")
    public ResponseEntity<List<Map<String, Object>>> searchAssets(
            @RequestParam(defaultValue = "") String q,
            @RequestParam(defaultValue = "100") int limit
    ) {
        int safeLimit = Math.clamp(limit, 1, 200);

        List<Asset> assets = q.isBlank()
                ? assetRepository.findAll().stream().limit(safeLimit).toList()
                : assetRepository.findBySymbolContainingIgnoreCaseOrNameContainingIgnoreCase(q, q)
                .stream().limit(safeLimit).toList();

        List<Map<String, Object>> result = assets.stream()
                .map(a -> Map.<String, Object>of(
                        "symbol", a.getSymbol(),
                        "name", a.getName(),
                        "category", a.getCategory(),
                        "providerId", a.getProviderId()
                ))
                .toList();

        return ResponseEntity.ok(result);
    }

    // 2) Preço único (ok)
    @GetMapping("/price")
    public ResponseEntity<Map<String, Object>> getPrice(
            @RequestParam String symbol,
            @RequestParam(defaultValue = "USD") String currency
    ) {
        String sym = symbol.toUpperCase();
        String cur = currency.toUpperCase();

        Asset asset = assetRepository.findBySymbol(sym).orElse(null);
        if (asset == null) return ResponseEntity.notFound().build();

        BigDecimal price = marketDataProvider.getCurrentPrice(asset.getProviderId(), cur);
        return ResponseEntity.ok(Map.of("symbol", sym, "currency", cur, "price", price));
    }

    // 3) ✅ Preços em lote (falta hoje)
    @GetMapping("/prices")
    public ResponseEntity<Map<String, Object>> getPrices(
            @RequestParam String symbols,
            @RequestParam(defaultValue = "USD") String currency
    ) {
        String cur = currency.toUpperCase();

        List<String> symbolList = Arrays.stream(symbols.split(","))
                .map(String::trim)
                .filter(s -> !s.isBlank())
                .map(String::toUpperCase)
                .distinct()
                .limit(100)
                .toList();

        List<Asset> assets = symbolList.stream()
                .map(assetRepository::findBySymbol)
                .filter(Optional::isPresent)
                .map(Optional::get)
                .toList();

        List<String> providerIds = assets.stream().map(Asset::getProviderId).toList();
        Map<String, BigDecimal> pricesByProviderId = marketDataProvider.getMultiplePrices(providerIds, cur);

        // volta no formato symbol -> price
        Map<String, BigDecimal> pricesBySymbol = assets.stream()
                .collect(java.util.stream.Collectors.toMap(
                        Asset::getSymbol,
                        a -> pricesByProviderId.getOrDefault(a.getProviderId(), BigDecimal.ZERO)
                ));

        return ResponseEntity.ok(Map.of(
                "currency", cur,
                "prices", pricesBySymbol
        ));
    }

    // 4) History com limite (evita days absurdo)
    @GetMapping("/history")
    public ResponseEntity<List<MarketDataProvider.PricePoint>> getHistory(
            @RequestParam String symbol,
            @RequestParam(defaultValue = "USD") String currency,
            @RequestParam(defaultValue = "90") int days
    ) {
        String sym = symbol.toUpperCase();
        String cur = currency.toUpperCase();

        int safeDays = Math.min(Math.max(days, 1), 365);

        Asset asset = assetRepository.findBySymbol(sym).orElse(null);
        if (asset == null) return ResponseEntity.notFound().build();

        List<MarketDataProvider.PricePoint> history =
                marketDataProvider.getPriceHistoryWithDates(asset.getProviderId(), cur, safeDays);

        return ResponseEntity.ok(history);
    }
}

