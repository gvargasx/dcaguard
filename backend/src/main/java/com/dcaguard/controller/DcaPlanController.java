package com.dcaguard.controller;

import com.dcaguard.dto.request.AddExecutionRequest;
import com.dcaguard.dto.request.CreateDcaPlanRequest;
import com.dcaguard.dto.response.AlertResponse;
import com.dcaguard.dto.response.DcaPlanResponse;
import com.dcaguard.dto.response.DcaSummaryResponse;
import com.dcaguard.security.SecurityUtils;
import com.dcaguard.service.impl.DcaPlanService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/dca/plans")
public class DcaPlanController {

    private final DcaPlanService dcaPlanService;

    public DcaPlanController(DcaPlanService dcaPlanService) {
        this.dcaPlanService = dcaPlanService;
    }

    @PostMapping
    public ResponseEntity<DcaPlanResponse> create(@Valid @RequestBody CreateDcaPlanRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(dcaPlanService.createPlan(SecurityUtils.getCurrentUserId(), request));
    }

    @GetMapping
    public ResponseEntity<List<DcaPlanResponse>> list() {
        return ResponseEntity.ok(dcaPlanService.getUserPlans(SecurityUtils.getCurrentUserId()));
    }

    @GetMapping("/{id}")
    public ResponseEntity<DcaPlanResponse> get(@PathVariable Long id) {
        return ResponseEntity.ok(dcaPlanService.getPlan(SecurityUtils.getCurrentUserId(), id));
    }

    @PostMapping("/{id}/executions")
    public ResponseEntity<DcaSummaryResponse.ExecutionResponse> addExecution(
            @PathVariable Long id, @Valid @RequestBody AddExecutionRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(dcaPlanService.addExecution(SecurityUtils.getCurrentUserId(), id, request));
    }

    @GetMapping("/{id}/summary")
    public ResponseEntity<DcaSummaryResponse> summary(@PathVariable Long id) {
        return ResponseEntity.ok(dcaPlanService.getSummary(SecurityUtils.getCurrentUserId(), id));
    }

    @GetMapping("/{id}/alerts")
    public ResponseEntity<List<AlertResponse>> alerts(@PathVariable Long id) {
        return ResponseEntity.ok(dcaPlanService.getAlerts(SecurityUtils.getCurrentUserId(), id));
    }
}
