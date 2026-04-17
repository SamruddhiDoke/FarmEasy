package com.frameasy.controller;

import com.frameasy.dto.ApiResponse;
import com.frameasy.dto.EquipmentDto;
import com.frameasy.security.UserPrincipal;
import com.frameasy.service.EquipmentService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.List;

/**
 * Equipment REST API.
 * Public listing: when lat/lon are not provided, returns ALL equipment (no distance filter).
 * When lat/lon are provided, filters by radius (default 100 km).
 */
@RestController
@RequestMapping("/equipment")
@RequiredArgsConstructor
public class EquipmentController {

    private static final double DEFAULT_RADIUS_KM = 100.0;

    private final EquipmentService equipmentService;

    /**
     * List equipment for browsing. Safe filtering:
     * - If lat or lon is null → returns ALL approved equipment (no location filter).
     * - If both lat and lon provided → filters by distance, default radius 100 km.
     */
    @GetMapping("/public")
    public ResponseEntity<ApiResponse<List<EquipmentDto>>> listPublic(
            @RequestParam(required = false) String category,
            @RequestParam(required = false) BigDecimal minPrice,
            @RequestParam(required = false) BigDecimal maxPrice,
            @RequestParam(required = false) Double lat,
            @RequestParam(required = false) Double lon,
            @RequestParam(required = false) Double radiusKm) {
        double effectiveRadius = (radiusKm != null && radiusKm > 0) ? radiusKm : DEFAULT_RADIUS_KM;
        List<EquipmentDto> list = equipmentService.listPublic(category, minPrice, maxPrice, lat, lon, effectiveRadius);
        if (list == null) list = Collections.emptyList();
        return ResponseEntity.ok(ApiResponse.success(list));
    }

    @GetMapping("/my")
    public ResponseEntity<ApiResponse<List<EquipmentDto>>> myList(@AuthenticationPrincipal UserPrincipal principal) {
        if (principal == null) return ResponseEntity.status(401).build();
        return ResponseEntity.ok(ApiResponse.success(equipmentService.listByUser(principal.getId())));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<EquipmentDto>> getById(@PathVariable Long id) {
        EquipmentDto dto = equipmentService.getById(id);
        if (dto == null) return ResponseEntity.notFound().build();
        return ResponseEntity.ok(ApiResponse.success(dto));
    }

    @PostMapping
    public ResponseEntity<ApiResponse<EquipmentDto>> create(@AuthenticationPrincipal UserPrincipal principal,
                                                           @RequestBody EquipmentDto dto) {
        if (principal == null) return ResponseEntity.status(401).build();
        return ResponseEntity.ok(ApiResponse.success(equipmentService.create(principal.getId(), dto)));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<EquipmentDto>> update(@AuthenticationPrincipal UserPrincipal principal,
                                                            @PathVariable Long id, @RequestBody EquipmentDto dto) {
        if (principal == null) return ResponseEntity.status(401).build();
        EquipmentDto updated = equipmentService.update(id, principal.getId(), dto);
        if (updated == null) return ResponseEntity.notFound().build();
        return ResponseEntity.ok(ApiResponse.success(updated));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> delete(@AuthenticationPrincipal UserPrincipal principal, @PathVariable Long id) {
        if (principal == null) return ResponseEntity.status(401).build();
        boolean ok = equipmentService.delete(id, principal.getId());
        if (!ok) return ResponseEntity.notFound().build();
        return ResponseEntity.ok(ApiResponse.success(null));
    }
}
