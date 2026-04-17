package com.frameasy.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
public class AgreementRequest {
    @NotBlank
    private String agreementType; // EQUIPMENT, LAND, TRADE
    @NotNull
    private Long referenceId;
    @NotBlank
    private String buyerName;
    @NotNull
    private BigDecimal finalPrice;
    private LocalDate dueDate;
    private String terms;
    @NotBlank
    private String otp;
}
