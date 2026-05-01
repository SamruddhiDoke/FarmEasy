package com.frameasy.model;

import jakarta.persistence.*;
import lombok.*;

import java.time.Instant;
import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "users")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;

    @Column(unique = true, nullable = false)
    private String email;

    private String phone;
    @Column(nullable = false)
    private String password;

    private String location;
    private Double latitude;
    private Double longitude;

    @Column(name = "preferred_language", length = 10)
    @Builder.Default
    private String preferredLanguage = "en";

    @Column(name = "farm_size")
    private String farmSize;

    @Column(name = "equipment_owned", columnDefinition = "TEXT")
    private String equipmentOwned;

    @Column(name = "land_details", columnDefinition = "TEXT")
    private String landDetails;

    @Column(columnDefinition = "TEXT")
    private String address;

    @Column(name = "is_verified")
    @Builder.Default
    private Boolean isVerified = false;

    @Column(name = "is_active")
    @Builder.Default
    private Boolean isActive = true;

    @Column(name = "created_at")
    @Builder.Default
    private Instant createdAt = Instant.now();

    @Column(name = "updated_at")
    @Builder.Default
    private Instant updatedAt = Instant.now();

    // Roles are managed separately; avoid cascading PERSIST to prevent "detached entity passed to persist"
    @ManyToMany(fetch = FetchType.EAGER, cascade = {CascadeType.MERGE})
    @JoinTable(
            name = "user_roles",
            joinColumns = @JoinColumn(name = "user_id"),
            inverseJoinColumns = @JoinColumn(name = "role_id")
    )
    @Builder.Default
    private Set<Role> roles = new HashSet<>();
}
