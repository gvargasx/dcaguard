package com.dcaguard.repository;

import com.dcaguard.entity.DcaPlan;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;

public interface DcaPlanRepository extends JpaRepository<DcaPlan, Long> {
    List<DcaPlan> findByUserId(Long userId);
    Optional<DcaPlan> findByIdAndUserId(Long id, Long userId);
    long countByUserId(Long userId);
}
