package com.frameasy.controller;

import com.frameasy.dto.ApiResponse;
import com.frameasy.dto.LandDto;
import com.frameasy.security.UserPrincipal;
import com.frameasy.service.LandService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Land listing API. When lat/lon are not provided, returns ALL approved land.
 * When lat/lon provided, filters by radius (default 100 km).
 */
@RestController
@RequestMapping("/land")
@RequiredArgsConstructor
public class LandController {

    private static final double DEFAULT_RADIUS_KM = 100.0;

    private final LandService landService;

    @GetMapping("/public")
    public ResponseEntity<ApiResponse<List<LandDto>>> listPublic(
            @RequestParam(required = false) Double lat,
            @RequestParam(required = false) Double lon,
            @RequestParam(required = false) Double radiusKm,
            @RequestParam(required = false) String search) {
        double effectiveRadius = (radiusKm != null && radiusKm > 0) ? radiusKm : DEFAULT_RADIUS_KM;
        List<LandDto> list = landService.listPublic(lat, lon, effectiveRadius, search);
        return ResponseEntity.ok(ApiResponse.success(list));
    }

    @GetMapping("/my")
    public ResponseEntity<ApiResponse<List<LandDto>>> myList(@AuthenticationPrincipal UserPrincipal principal) {
        if (principal == null) return ResponseEntity.status(401).build();
        return ResponseEntity.ok(ApiResponse.success(landService.listByUser(principal.getId())));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<LandDto>> getById(@PathVariable Long id) {
        LandDto dto = landService.getById(id);
        if (dto == null) return ResponseEntity.notFound().build();
        return ResponseEntity.ok(ApiResponse.success(dto));
    }

    @PostMapping
    public ResponseEntity<ApiResponse<LandDto>> create(@AuthenticationPrincipal UserPrincipal principal,
                                                       @RequestBody LandDto dto) {
        if (principal == null) return ResponseEntity.status(401).build();
        return ResponseEntity.ok(ApiResponse.success(landService.create(principal.getId(), dto)));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<LandDto>> update(@AuthenticationPrincipal UserPrincipal principal,
                                                        @PathVariable Long id, @RequestBody LandDto dto) {
        if (principal == null) return ResponseEntity.status(401).build();
        LandDto updated = landService.update(id, principal.getId(), dto);
        if (updated == null) return ResponseEntity.notFound().build();
        return ResponseEntity.ok(ApiResponse.success(updated));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> delete(@AuthenticationPrincipal UserPrincipal principal, @PathVariable Long id) {
        if (principal == null) return ResponseEntity.status(401).build();
        boolean ok = landService.delete(id, principal.getId());
        if (!ok) return ResponseEntity.notFound().build();
        return ResponseEntity.ok(ApiResponse.success(null));
    }
}
