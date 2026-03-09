package com.dcaguard.service.market;

import com.dcaguard.entity.Asset;
import com.dcaguard.repository.AssetRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Locale;

@Service
public class AssetSyncService {

    private final MarketDataProvider marketDataProvider;
    private final AssetRepository assetRepository;

    public AssetSyncService(MarketDataProvider marketDataProvider,
                            AssetRepository assetRepository) {
        this.marketDataProvider = marketDataProvider;
        this.assetRepository = assetRepository;
    }

    @Transactional
    public int syncTopAssets(int limit, String currency) {
        List<MarketDataProvider.AssetSnapshot> topAssets =
                marketDataProvider.getTopAssets(limit, currency);

        for (MarketDataProvider.AssetSnapshot snapshot : topAssets) {

            Asset asset = assetRepository
                    .findByProviderId(snapshot.providerId())
                    .orElse(null);

            if (asset == null) {
                // novo ativo
                asset = new Asset();
                asset.setSymbol(snapshot.symbol().toUpperCase(Locale.ROOT));
                asset.setProviderId(snapshot.providerId());
                asset.setName(snapshot.name());
                asset.setCategory(resolveCategory(snapshot.symbol()));
                asset.setImageUrl(snapshot.imageUrl());
            } else {
                // atualiza dados (caso nome ou imagem mudem)
                asset.setName(snapshot.name());
                asset.setImageUrl(snapshot.imageUrl());
                asset.setSymbol(snapshot.symbol().toUpperCase(Locale.ROOT));
            }

            assetRepository.save(asset);
        }

        return topAssets.size();
    }

    private String resolveCategory(String symbol) {
        String s = symbol.toUpperCase();

        return switch (s) {
            case "USDT", "USDC", "DAI" -> "STABLECOIN";
            case "BTC", "ETH", "SOL", "BNB", "ADA", "AVAX" -> "LAYER1";
            case "MATIC", "ARB", "OP" -> "LAYER2";
            case "UNI", "AAVE", "LINK" -> "DEFI";
            case "DOGE", "SHIB", "PEPE" -> "MEME";
            default -> "GENERAL";
        };

    }
}