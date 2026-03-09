package com.dcaguard.service.market;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
public class AssetSyncJob {

    private final AssetSyncService assetSyncService;

    public AssetSyncJob(AssetSyncService assetSyncService) {
        this.assetSyncService = assetSyncService;
    }

    @Scheduled(initialDelay = 10_000, fixedDelay = 30 * 60_000) // a cada 30 min
    public void syncTop100() {
        assetSyncService.syncTopAssets(100, "USD");
    }
}
