package com.frameasy.repository;

import com.frameasy.model.Agreement;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface AgreementRepository extends JpaRepository<Agreement, Long> {
    List<Agreement> findBySellerId(Long sellerId);
    List<Agreement> findByBuyerId(Long buyerId);
    List<Agreement> findBySellerIdOrBuyerId(Long sellerId, Long buyerId);
}
