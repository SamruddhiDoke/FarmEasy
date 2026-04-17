package com.frameasy.repository;

import com.frameasy.model.OtpVerification;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.Instant;
import java.util.Optional;

public interface OtpVerificationRepository extends JpaRepository<OtpVerification, Long> {
    Optional<OtpVerification> findFirstByEmailAndPurposeAndUsedFalseAndExpiresAtAfterOrderByCreatedAtDesc(
            String email, String purpose, Instant now);
}
