package com.dcaguard.repository;

import com.dcaguard.entity.DcaExecution;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface DcaExecutionRepository extends JpaRepository<DcaExecution, Long> {
    List<DcaExecution> findByPlanIdOrderByExecutionDateAsc(Long planId);
}
