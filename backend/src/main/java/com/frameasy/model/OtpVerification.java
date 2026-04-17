package com.frameasy.model;

import jakarta.persistence.*;
import lombok.*;

import java.time.Instant;

@Entity
@Table(name = "otp_verification")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OtpVerification {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String email;

    @Column(nullable = false, length = 10)
    private String otp;

    @Column(nullable = false, length = 50)
    private String purpose;

    @Column(name = "expires_at", nullable = false)
    private Instant expiresAt;

    @Builder.Default
    private boolean used = false;

    @Column(name = "created_at")
    private Instant createdAt = Instant.now();
}
