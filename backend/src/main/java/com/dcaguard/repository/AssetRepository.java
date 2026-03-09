package com.dcaguard.repository;

import com.dcaguard.entity.Asset;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface AssetRepository extends JpaRepository<Asset, Long> {
    Optional<Asset> findBySymbol(String symbol);

    List<Asset> findBySymbolContainingIgnoreCaseOrNameContainingIgnoreCase(String symbol, String name);

    Optional<Asset> findByProviderId(String providerId);
}
