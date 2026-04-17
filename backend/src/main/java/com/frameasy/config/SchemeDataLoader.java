package com.frameasy.config;

import com.frameasy.service.SchemeService;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class SchemeDataLoader implements ApplicationRunner {

    private final SchemeService schemeService;

    @Override
    public void run(ApplicationArguments args) {
        schemeService.seedIfEmpty();
    }
}
