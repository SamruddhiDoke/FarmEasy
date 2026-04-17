package com.frameasy.repository;

import com.frameasy.model.Land;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.repository.query.Param;

import java.math.BigDecimal;
import java.util.List;

public interface LandRepository extends JpaRepository<Land, Long> {
    List<Land> findByUserIdAndIsActiveTrue(Long userId);
    List<Land> findByIsApprovedTrueAndIsActiveTrue();
    List<Land> findByIsApprovedTrueAndIsActiveTrueAndLocationContaining(String location);
}
