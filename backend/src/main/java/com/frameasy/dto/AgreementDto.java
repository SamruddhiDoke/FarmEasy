package com.frameasy.dto;

import lombok.Data;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;

@Data
public class AgreementDto {
    private Long id;
    private String agreementType;
    private Long referenceId;
    private Long sellerId;
    private Long buyerId;
    private String buyerName;
    private BigDecimal finalPrice;
    private LocalDate dueDate;
    private String terms;
    private String pdfPath;
    private Instant signedAt;
    private String downloadUrl;
}
