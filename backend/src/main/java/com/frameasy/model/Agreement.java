package com.frameasy.model;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;

@Entity
@Table(name = "agreements")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Agreement {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "agreement_type", nullable = false, length = 50)
    private String agreementType; // EQUIPMENT, LAND, TRADE

    @Column(name = "reference_id", nullable = false)
    private Long referenceId;

    @Column(name = "seller_id", nullable = false)
    private Long sellerId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "seller_id", insertable = false, updatable = false)
    private User seller;

    @Column(name = "buyer_id", nullable = false)
    private Long buyerId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "buyer_id", insertable = false, updatable = false)
    private User buyer;

    @Column(name = "buyer_name")
    private String buyerName;

    @Column(name = "final_price", nullable = false, precision = 12, scale = 2)
    private BigDecimal finalPrice;

    @Column(name = "due_date")
    private LocalDate dueDate;

    @Column(columnDefinition = "TEXT")
    private String terms;

    @Column(name = "pdf_path")
    private String pdfPath;

    @Column(name = "signed_at")
    @Builder.Default
    private Instant signedAt = Instant.now();
}
