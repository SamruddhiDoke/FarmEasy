package com.frameasy.controller;

import com.frameasy.dto.AgreementRequest;
import com.frameasy.dto.AgreementDto;
import com.frameasy.dto.ApiResponse;
import com.frameasy.security.UserPrincipal;
import com.frameasy.service.AgreementService;
import com.frameasy.service.OtpService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/agreements")
@RequiredArgsConstructor
public class AgreementController {

    private final AgreementService agreementService;
    private final OtpService otpService;

    @PostMapping("/send-otp")
    public ResponseEntity<ApiResponse<Void>> sendOtp(@AuthenticationPrincipal UserPrincipal principal,
                                                     @RequestParam String email) {
        if (principal == null) return ResponseEntity.status(401).build();
        agreementService.sendAgreementOtp(email);
        return ResponseEntity.ok(ApiResponse.success("OTP sent", null));
    }

    @PostMapping
    public ResponseEntity<ApiResponse<AgreementDto>> create(@AuthenticationPrincipal UserPrincipal principal,
                                                             @Valid @RequestBody AgreementRequest request) {
        if (principal == null) return ResponseEntity.status(401).build();
        AgreementDto dto = agreementService.createAgreement(principal.getId(), request);
        if (dto == null) {
            return ResponseEntity.badRequest().body(ApiResponse.error("Invalid OTP or reference"));
        }
        return ResponseEntity.ok(ApiResponse.success(dto));
    }

    @GetMapping("/my")
    public ResponseEntity<ApiResponse<List<AgreementDto>>> myList(@AuthenticationPrincipal UserPrincipal principal) {
        if (principal == null) return ResponseEntity.status(401).build();
        return ResponseEntity.ok(ApiResponse.success(agreementService.listByUser(principal.getId())));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<AgreementDto>> getById(@AuthenticationPrincipal UserPrincipal principal,
                                                            @PathVariable Long id) {
        if (principal == null) return ResponseEntity.status(401).build();
        AgreementDto dto = agreementService.getById(id);
        if (dto == null) return ResponseEntity.notFound().build();
        if (!dto.getSellerId().equals(principal.getId()) && !dto.getBuyerId().equals(principal.getId())) {
            return ResponseEntity.status(403).build();
        }
        return ResponseEntity.ok(ApiResponse.success(dto));
    }
}
