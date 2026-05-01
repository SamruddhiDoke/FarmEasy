package com.frameasy.config;

import com.frameasy.model.Role;
import com.frameasy.repository.RoleRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * Seeds the roles table on application startup if roles are missing.
 * This ensures the application works correctly on a fresh database.
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class DataInitializer implements ApplicationRunner {

    private final RoleRepository roleRepository;

    private static final List<String> REQUIRED_ROLES = List.of(
            "ROLE_ADMIN",
            "ROLE_FARMER",
            "ROLE_CUSTOMER"
    );

    @Override
    @Transactional
    public void run(ApplicationArguments args) {
        for (String roleName : REQUIRED_ROLES) {
            if (!roleRepository.existsByName(roleName)) {
                roleRepository.save(Role.builder().name(roleName).build());
                log.info("Seeded role: {}", roleName);
            }
        }
        log.info("Role seeding complete. {} roles verified.", REQUIRED_ROLES.size());
    }
}
