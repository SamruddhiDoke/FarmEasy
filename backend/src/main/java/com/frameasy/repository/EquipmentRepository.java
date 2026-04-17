package com.frameasy.repository;

import com.frameasy.model.Equipment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface EquipmentRepository extends JpaRepository<Equipment, Long> {
    List<Equipment> findByUserIdAndIsActiveTrue(Long userId);
    List<Equipment> findByIsApprovedTrueAndIsActiveTrue();
    // For demos/evaluation: show all active equipment regardless of approval
    List<Equipment> findByIsActiveTrue();

    @Query(value = "SELECT e FROM Equipment e WHERE e.isApproved = true AND e.isActive = true " +
            "AND (:category IS NULL OR e.category = :category) " +
            "AND (:minPrice IS NULL OR e.pricePerDay >= :minPrice) " +
            "AND (:maxPrice IS NULL OR e.pricePerDay <= :maxPrice)")
    List<Equipment> search(@Param("category") String category,
                           @Param("minPrice") java.math.BigDecimal minPrice,
                           @Param("maxPrice") java.math.BigDecimal maxPrice);
}
