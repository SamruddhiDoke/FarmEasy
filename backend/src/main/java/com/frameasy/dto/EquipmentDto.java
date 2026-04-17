package com.frameasy.dto;

import lombok.Data;

import java.math.BigDecimal;
import java.time.Instant;

@Data
public class EquipmentDto {
    private Long id;
    private Long userId;
    private String ownerName;
    private String title;
    private String description;
    private BigDecimal pricePerDay;
    private String imageUrl;
    private String category;
    private String availability;
    private String location;
    private Double latitude;
    private Double longitude;
    private Boolean isApproved;
    private Boolean isActive;
    private Instant createdAt;
    private Double distanceKm; // optional, when filtered by location
}
