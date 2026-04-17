package com.frameasy.dto;

import lombok.Data;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

@Data
public class OrderDto {
    private Long id;
    private Long userId;
    private String fullName;
    private String address;
    private String city;
    private String state;
    private String pincode;
    private String phone;
    private BigDecimal totalAmount;
    private String status;
    private Instant createdAt;
    private List<Item> items;

    @Data
    public static class Item {
        private String type;
        private Long referenceId;
        private String title;
        private BigDecimal unitPrice;
        private Integer qty;
    }
}

