package com.frameasy.controller;

import com.frameasy.dto.*;
import com.frameasy.security.UserPrincipal;
import com.frameasy.service.AuthService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    @PostMapping("/register")
    public ResponseEntity<ApiResponse<AuthResponse>> register(@Valid @RequestBody RegisterRequest request) {
        return ResponseEntity.ok(authService.register(request));
    }

    @PostMapping("/verify-registration")
    public ResponseEntity<ApiResponse<AuthResponse>> verifyRegistration(@Valid @RequestBody OtpRequest request) {
        return ResponseEntity.ok(authService.verifyRegistration(request));
    }

    @PostMapping("/login")
    public ResponseEntity<ApiResponse<AuthResponse>> login(@Valid @RequestBody LoginRequest request) {
        return ResponseEntity.ok(authService.login(request));
    }

    @PostMapping("/verify-login")
    public ResponseEntity<ApiResponse<AuthResponse>> verifyLogin(@Valid @RequestBody OtpRequest request) {
        return ResponseEntity.ok(authService.verifyLogin(request));
    }

    @GetMapping("/me")
    public ResponseEntity<ApiResponse<AuthResponse>> me(@AuthenticationPrincipal UserPrincipal principal) {
        if (principal == null) {
            return ResponseEntity.status(401).body(ApiResponse.error("Unauthorized"));
        }
        return ResponseEntity.ok(authService.getCurrentUser(principal));
    }

    @PostMapping("/resend-otp")
    public ResponseEntity<ApiResponse<Void>> resendOtp(@RequestBody OtpRequest request) {
        authService.resendOtp(request.getEmail(), request.getPurpose());
        return ResponseEntity.ok(ApiResponse.success("OTP sent", null));
    }
}
