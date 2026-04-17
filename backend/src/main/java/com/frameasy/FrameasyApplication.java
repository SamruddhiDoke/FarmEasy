package com.frameasy;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * FARM EASY - Main Spring Boot Application.
 * Enables scheduling for scheme cache refresh.
 */
@SpringBootApplication
@EnableScheduling
public class FrameasyApplication {

    public static void main(String[] args) {
        SpringApplication.run(FrameasyApplication.class, args);
    }
}
