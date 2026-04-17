package com.frameasy.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AuthResponse {
    private String token;
    private String type = "Bearer";
    private Long id;
    private String email;
    private String name;
    private List<String> roles;
    private String preferredLanguage;
    private boolean requiresOtp;
    // Profile fields (for GET profile)
    private String phone;
    private String location;
    private String address;
    private String farmSize;
    private String equipmentOwned;
    private String landDetails;
}
