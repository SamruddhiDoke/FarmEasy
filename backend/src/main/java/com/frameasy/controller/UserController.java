package com.frameasy.controller;

import com.frameasy.dto.ApiResponse;
import com.frameasy.dto.AuthResponse;
import com.frameasy.model.User;
import com.frameasy.repository.UserRepository;
import com.frameasy.security.UserPrincipal;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/users")
@RequiredArgsConstructor
public class UserController {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @GetMapping("/profile")
    public ResponseEntity<ApiResponse<AuthResponse>> getProfile(@AuthenticationPrincipal UserPrincipal principal) {
        if (principal == null) return ResponseEntity.status(401).build();
        User user = userRepository.findById(principal.getId()).orElse(null);
        if (user == null) return ResponseEntity.notFound().build();
        AuthResponse res = AuthResponse.builder()
                .id(user.getId())
                .email(user.getEmail())
                .name(user.getName())
                .preferredLanguage(user.getPreferredLanguage())
                .phone(user.getPhone())
                .location(user.getLocation())
                .address(user.getAddress())
                .farmSize(user.getFarmSize())
                .equipmentOwned(user.getEquipmentOwned())
                .landDetails(user.getLandDetails())
                .build();
        res.setRoles(user.getRoles().stream().map(r -> r.getName()).toList());
        return ResponseEntity.ok(ApiResponse.success(res));
    }

    @PutMapping("/profile")
    public ResponseEntity<ApiResponse<AuthResponse>> updateProfile(@AuthenticationPrincipal UserPrincipal principal,
                                                                   @RequestBody ProfileUpdateRequest req) {
        if (principal == null) return ResponseEntity.status(401).build();
        User user = userRepository.findById(principal.getId()).orElse(null);
        if (user == null) return ResponseEntity.notFound().build();
        if (req.getName() != null) user.setName(req.getName());
        if (req.getPhone() != null) user.setPhone(req.getPhone());
        if (req.getLocation() != null) user.setLocation(req.getLocation());
        if (req.getAddress() != null) user.setAddress(req.getAddress());
        if (req.getFarmSize() != null) user.setFarmSize(req.getFarmSize());
        if (req.getEquipmentOwned() != null) user.setEquipmentOwned(req.getEquipmentOwned());
        if (req.getLandDetails() != null) user.setLandDetails(req.getLandDetails());
        if (req.getPreferredLanguage() != null) user.setPreferredLanguage(req.getPreferredLanguage());
        if (req.getLatitude() != null) user.setLatitude(req.getLatitude());
        if (req.getLongitude() != null) user.setLongitude(req.getLongitude());
        user = userRepository.save(user);
        AuthResponse res = AuthResponse.builder()
                .id(user.getId())
                .email(user.getEmail())
                .name(user.getName())
                .preferredLanguage(user.getPreferredLanguage())
                .phone(user.getPhone())
                .location(user.getLocation())
                .address(user.getAddress())
                .farmSize(user.getFarmSize())
                .equipmentOwned(user.getEquipmentOwned())
                .landDetails(user.getLandDetails())
                .build();
        res.setRoles(user.getRoles().stream().map(r -> r.getName()).toList());
        return ResponseEntity.ok(ApiResponse.success(res));
    }

    @lombok.Data
    public static class ProfileUpdateRequest {
        private String name;
        private String phone;
        private String location;
        private String address;
        private String farmSize;
        private String equipmentOwned;
        private String landDetails;
        private String preferredLanguage;
        private Double latitude;
        private Double longitude;
    }
}
