package com.dcaguard.repository;

import com.dcaguard.entity.PortfolioPosition;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface PortfolioPositionRepository extends JpaRepository<PortfolioPosition, Long> {
    Optional<PortfolioPosition> findByIdAndPortfolioId(Long id, Long portfolioId);
    Optional<PortfolioPosition> findByPortfolioIdAndAssetId(Long portfolioId, Long assetId);
}
