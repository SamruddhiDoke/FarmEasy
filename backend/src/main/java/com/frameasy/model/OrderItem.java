package com.frameasy.model;

import jakarta.persistence.Embeddable;
import lombok.*;

import java.math.BigDecimal;

@Embeddable
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OrderItem {
    private String type; // TRADE (future: EQUIPMENT)
    private Long referenceId;
    private String title;
    private BigDecimal unitPrice;
    private Integer qty;
}

