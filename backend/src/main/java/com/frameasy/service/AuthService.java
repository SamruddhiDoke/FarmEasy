package com.frameasy.service;

import com.frameasy.dto.*;
import com.frameasy.model.Role;
import com.frameasy.model.User;
import com.frameasy.repository.RoleRepository;
import com.frameasy.repository.UserRepository;
import com.frameasy.security.JwtUtil;
import com.frameasy.security.UserPrincipal;
import com.frameasy.util.InputNormalize;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final JwtUtil jwtUtil;
    private final OtpService otpService;

    @Transactional
    public ApiResponse<AuthResponse> register(RegisterRequest req) {
        String email = InputNormalize.email(req.getEmail());
        if (email == null || email.isEmpty()) {
            return ApiResponse.error("Valid email is required");
        }
        if (userRepository.existsByEmailIgnoreCase(email)) {
            return ApiResponse.error("Email already registered");
        }
        if (!req.getPassword().equals(req.getConfirmPassword())) {
            return ApiResponse.error("Passwords do not match");
        }
        Role role = roleRepository.findByName("ROLE_" + req.getRole().toUpperCase())
                .orElse(roleRepository.findByName("ROLE_FARMER").orElseThrow());
        User user = User.builder()
                .name(req.getName())
                .email(email)
                .phone(req.getPhone())
                .password(passwordEncoder.encode(req.getPassword()))
                .location(req.getLocation())
                .latitude(req.getLatitude())
                .longitude(req.getLongitude())
                .preferredLanguage(req.getPreferredLanguage() != null ? req.getPreferredLanguage() : "en")
                .isVerified(false)
                .build();
        user.getRoles().add(role);
        userRepository.save(user);
        otpService.createAndSendOtp(email, "REGISTRATION");
        return ApiResponse.success("OTP sent to email. Please verify.", AuthResponse.builder()
                .requiresOtp(true)
                .email(email)
                .build());
    }

    public ApiResponse<AuthResponse> verifyRegistration(OtpRequest req) {
        String email = InputNormalize.email(req.getEmail());
        if (!otpService.verifyOtp(email, req.getOtp(), "REGISTRATION")) {
            return ApiResponse.error("Invalid or expired OTP");
        }
        User user = userRepository.findByEmailIgnoreCase(email).orElse(null);
        if (user == null) return ApiResponse.error("User not found");
        user.setIsVerified(true);
        userRepository.save(user);
        String token = jwtUtil.generateTokenFromUserId(user.getId(), user.getEmail(),
                user.getRoles().stream().map(r -> r.getName()).collect(Collectors.toList()));
        return ApiResponse.success(AuthResponse.builder()
                .token(token)
                .id(user.getId())
                .email(user.getEmail())
                .name(user.getName())
                .roles(user.getRoles().stream().map(r -> r.getName()).collect(Collectors.toList()))
                .preferredLanguage(user.getPreferredLanguage())
                .requiresOtp(false)
                .build());
    }

    @Transactional
    public ApiResponse<AuthResponse> login(LoginRequest req) {
        // Demo-friendly admin login: allow a default admin user to log in without OTP
        // using a fixed email/password combination. This is ONLY for testing/demo.
        String adminEmail = "admin@farmeasy.com";
        String adminPassword = "admin123";
        String loginEmail = InputNormalize.email(req.getEmail());

        if (loginEmail != null && adminEmail.equals(loginEmail) && adminPassword.equals(req.getPassword())) {
            User admin = userRepository.findByEmailIgnoreCase(adminEmail).orElseGet(() -> {
                Role adminRole = roleRepository.findByName("ROLE_ADMIN")
                        .orElseGet(() -> roleRepository.save(Role.builder().name("ROLE_ADMIN").build()));
                User u = User.builder()
                        .name("Admin")
                        .email(adminEmail)
                        .password(passwordEncoder.encode(adminPassword))
                        .isVerified(true)
                        .isActive(true)
                        .preferredLanguage("en")
                        .roles(Set.of(adminRole))
                        .build();
                return userRepository.save(u);
            });
            List<String> roles = admin.getRoles().stream().map(Role::getName).collect(Collectors.toList());
            String token = jwtUtil.generateTokenFromUserId(admin.getId(), admin.getEmail(), roles);
            return ApiResponse.success(AuthResponse.builder()
                    .token(token)
                    .id(admin.getId())
                    .email(admin.getEmail())
                    .name(admin.getName())
                    .roles(roles)
                    .preferredLanguage(admin.getPreferredLanguage())
                    .requiresOtp(false)
                    .build());
        }

        if (loginEmail == null || loginEmail.isEmpty()) {
            return ApiResponse.error("Invalid email or password");
        }
        User user = userRepository.findByEmailIgnoreCase(loginEmail).orElse(null);
        if (user == null) {
            return ApiResponse.error("Invalid email or password");
        }
        if (!passwordEncoder.matches(req.getPassword(), user.getPassword())) {
            return ApiResponse.error("Invalid email or password");
        }
        if (!user.getIsActive()) {
            return ApiResponse.error("Account is deactivated");
        }
        otpService.createAndSendOtp(loginEmail, "LOGIN");
        return ApiResponse.success("OTP sent to email.", AuthResponse.builder()
                .requiresOtp(true)
                .email(loginEmail)
                .build());
    }

    public ApiResponse<AuthResponse> verifyLogin(OtpRequest req) {
        String email = InputNormalize.email(req.getEmail());
        if (!otpService.verifyOtp(email, req.getOtp(), "LOGIN")) {
            return ApiResponse.error("Invalid or expired OTP");
        }
        User user = userRepository.findByEmailIgnoreCase(email).orElse(null);
        if (user == null) return ApiResponse.error("User not found");
        List<String> roles = user.getRoles().stream().map(Role::getName).collect(Collectors.toList());
        String token = jwtUtil.generateTokenFromUserId(user.getId(), user.getEmail(), roles);
        return ApiResponse.success(AuthResponse.builder()
                .token(token)
                .id(user.getId())
                .email(user.getEmail())
                .name(user.getName())
                .roles(roles)
                .preferredLanguage(user.getPreferredLanguage())
                .requiresOtp(false)
                .build());
    }

    public ApiResponse<AuthResponse> getCurrentUser(UserPrincipal principal) {
        if (principal == null) return ApiResponse.error("Unauthorized");
        User user = userRepository.findById(principal.getId()).orElse(null);
        if (user == null) return ApiResponse.error("User not found");
        List<String> roles = user.getRoles().stream().map(Role::getName).collect(Collectors.toList());
        return ApiResponse.success(AuthResponse.builder()
                .id(user.getId())
                .email(user.getEmail())
                .name(user.getName())
                .roles(roles)
                .preferredLanguage(user.getPreferredLanguage())
                .build());
    }

    public void resendOtp(String email, String purpose) {
        String normalized = InputNormalize.email(email);
        if (normalized == null || normalized.isEmpty()) {
            return;
        }
        if (purpose == null || purpose.isBlank()) {
            purpose = "REGISTRATION";
        }
        otpService.createAndSendOtp(normalized, purpose);
    }
}
