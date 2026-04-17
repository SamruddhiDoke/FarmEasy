package com.frameasy.dto;

import lombok.Data;

import java.math.BigDecimal;
import java.time.Instant;

@Data
public class LandDto {
    private Long id;
    private Long userId;
    private String ownerName;
    private String title;
    private String description;
    private BigDecimal pricePerMonth;
    private String imageUrl;
    private String area;
    private String location;
    private Double latitude;
    private Double longitude;
    private Boolean isApproved;
    private Boolean isActive;
    private Instant createdAt;
    private Double distanceKm;
}
