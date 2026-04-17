package com.frameasy.dto;

import lombok.Data;

import java.math.BigDecimal;
import java.time.Instant;

@Data
public class TradeDto {
    private Long id;
    private Long userId;
    private String sellerName;
    private String cropName;
    private String description;
    private BigDecimal pricePerUnit;
    private String unit;
    private String quantity;
    private String imageUrl;
    private String location;
    private Double latitude;
    private Double longitude;
    private Boolean isApproved;
    private Boolean isActive;
    private Instant createdAt;
    private Double distanceKm;
}
