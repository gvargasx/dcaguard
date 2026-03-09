package com.dcaguard.controller;

import com.dcaguard.dto.request.AddPositionRequest;
import com.dcaguard.dto.request.CreatePortfolioRequest;
import com.dcaguard.dto.response.PortfolioAnalysisResponse;
import com.dcaguard.dto.response.PortfolioResponse;
import com.dcaguard.dto.response.PositionResponse;
import com.dcaguard.security.SecurityUtils;
import com.dcaguard.service.impl.PortfolioService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/portfolios")
public class PortfolioController {

    private final PortfolioService portfolioService;

    public PortfolioController(PortfolioService portfolioService) {
        this.portfolioService = portfolioService;
    }

    @PostMapping
    public ResponseEntity<PortfolioResponse> create(@Valid @RequestBody CreatePortfolioRequest request) {
        Long userId = SecurityUtils.getCurrentUserId();
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(portfolioService.createPortfolio(userId, request));
    }

    @GetMapping
    public ResponseEntity<List<PortfolioResponse>> list() {
        return ResponseEntity.ok(portfolioService.getUserPortfolios(SecurityUtils.getCurrentUserId()));
    }

    @GetMapping("/{id}")
    public ResponseEntity<PortfolioResponse> get(@PathVariable Long id) {
        return ResponseEntity.ok(portfolioService.getPortfolio(SecurityUtils.getCurrentUserId(), id));
    }

    @PostMapping("/{id}/positions")
    public ResponseEntity<PositionResponse> addPosition(
            @PathVariable Long id, @Valid @RequestBody AddPositionRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(portfolioService.addPosition(SecurityUtils.getCurrentUserId(), id, request));
    }

    @DeleteMapping("/{id}/positions/{positionId}")
    public ResponseEntity<Void> deletePosition(@PathVariable Long id, @PathVariable Long positionId) {
        portfolioService.deletePosition(SecurityUtils.getCurrentUserId(), id, positionId);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/{id}/analysis")
    public ResponseEntity<PortfolioAnalysisResponse> analyze(@PathVariable Long id) {
        return ResponseEntity.ok(portfolioService.analyzePortfolio(SecurityUtils.getCurrentUserId(), id));
    }
}
