package com.frameasy.repository;

import com.frameasy.model.Trade;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface TradeRepository extends JpaRepository<Trade, Long> {
    List<Trade> findByUserIdAndIsActiveTrue(Long userId);
    List<Trade> findByIsApprovedTrueAndIsActiveTrue();
    List<Trade> findByIsApprovedTrueAndIsActiveTrueAndCropNameContainingIgnoreCase(String cropName);
    // For demos/evaluation: show all active trade listings regardless of approval
    List<Trade> findByIsActiveTrue();
    List<Trade> findByIsActiveTrueAndCropNameContainingIgnoreCase(String cropName);
}
