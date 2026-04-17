package com.frameasy.service;

import com.frameasy.model.OtpVerification;
import com.frameasy.repository.OtpVerificationRepository;
import com.frameasy.util.InputNormalize;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.Optional;
import java.util.Random;

@Service
@RequiredArgsConstructor
public class OtpService {

    private final OtpVerificationRepository otpRepository;
    private final JavaMailSender mailSender;

    private static final int OTP_LENGTH = 6;
    private static final int OTP_VALID_MINUTES = 10;

    public String generateOtp() {
        Random r = new Random();
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < OTP_LENGTH; i++) {
            sb.append(r.nextInt(10));
        }
        return sb.toString();
    }

    public void createAndSendOtp(String email, String purpose) {
        String normalizedEmail = InputNormalize.email(email);
        String normalizedPurpose = InputNormalize.purpose(purpose);
        if (normalizedEmail == null || normalizedEmail.isEmpty()) {
            return;
        }
        String otp = generateOtp();
        String purposeStored = normalizedPurpose.isEmpty() ? "GENERAL" : normalizedPurpose;
        OtpVerification entity = OtpVerification.builder()
                .email(normalizedEmail)
                .otp(otp)
                .purpose(purposeStored)
                .expiresAt(Instant.now().plusSeconds(OTP_VALID_MINUTES * 60L))
                .build();
        otpRepository.save(entity);
        sendOtpEmail(normalizedEmail, otp, purposeStored);
    }

    @Async
    public void sendOtpEmail(String email, String otp, String purpose) {
        SimpleMailMessage msg = new SimpleMailMessage();
        msg.setTo(email);
        msg.setSubject("FARM EASY - OTP for " + (purpose == null ? "" : purpose));
        msg.setText("Your OTP is: " + otp + "\nValid for " + OTP_VALID_MINUTES + " minutes.");
        try {
            mailSender.send(msg);
        } catch (Exception e) {
            // log and optionally rethrow
        }
    }

    public boolean verifyOtp(String email, String otp, String purpose) {
        String normalizedEmail = InputNormalize.email(email);
        String normalizedOtp = InputNormalize.otp(otp);
        String normalizedPurpose = InputNormalize.purpose(purpose);
        if (normalizedEmail == null || normalizedEmail.isEmpty() || normalizedOtp.isEmpty()) {
            return false;
        }
        if (normalizedPurpose.isEmpty()) {
            return false;
        }
        Optional<OtpVerification> opt = otpRepository
                .findFirstByEmailAndPurposeAndUsedFalseAndExpiresAtAfterOrderByCreatedAtDesc(
                        normalizedEmail, normalizedPurpose, Instant.now());
        if (opt.isEmpty() || !opt.get().getOtp().equals(normalizedOtp)) {
            return false;
        }
        OtpVerification o = opt.get();
        o.setUsed(true);
        otpRepository.save(o);
        return true;
    }
}
