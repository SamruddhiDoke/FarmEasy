package com.frameasy.controller;

import com.frameasy.dto.ApiResponse;
import com.frameasy.model.Scheme;
import com.frameasy.repository.SchemeRepository;
import com.frameasy.service.SchemeService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/schemes")
@RequiredArgsConstructor
public class SchemeController {

    private final SchemeService schemeService;
    private final SchemeRepository schemeRepository;

    @GetMapping("/public/list")
    public ResponseEntity<ApiResponse<List<Scheme>>> list(
            @RequestParam(required = false) String state,
            @RequestParam(required = false) String search) {
        List<Scheme> list = state != null && !state.isBlank()
                ? schemeService.listByState(state)
                : schemeService.listAll();
        if (search != null && !search.isBlank()) {
            list = schemeService.searchInList(list, search);
        }
        return ResponseEntity.ok(ApiResponse.success(list));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<Scheme>> getById(@PathVariable Long id) {
        return schemeRepository.findById(id)
                .map(s -> ResponseEntity.ok(ApiResponse.success(s)))
                .orElse(ResponseEntity.notFound().build());
    }
}
