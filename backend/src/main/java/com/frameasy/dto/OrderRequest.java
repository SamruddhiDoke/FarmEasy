package com.frameasy.dto;

import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

@Data
public class OrderRequest {
    private AddressDto address;
    private List<OrderItemDto> items;

    @Data
    public static class AddressDto {
        private String fullName;
        private String address;
        private String city;
        private String state;
        private String pincode;
        private String phone;
    }

    @Data
    public static class OrderItemDto {
        private String type;
        private Long referenceId;
        private String title;
        private BigDecimal unitPrice;
        private Integer qty;
    }
}

