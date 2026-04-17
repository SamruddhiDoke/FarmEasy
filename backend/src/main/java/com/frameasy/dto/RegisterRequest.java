package com.frameasy.dto;

import jakarta.validation.constraints.*;
import lombok.Data;

@Data
public class RegisterRequest {
    @NotBlank
    private String name;
    @NotBlank
    @Email
    private String email;
    private String phone;
    @Size(min = 6)
    private String password;
    private String confirmPassword;
    private String location;
    private Double latitude;
    private Double longitude;
    @Size(max = 10)
    private String preferredLanguage = "en";
    private String role; // FARMER or CUSTOMER
}
