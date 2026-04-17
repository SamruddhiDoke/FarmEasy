package com.frameasy.controller;

import com.frameasy.dto.ApiResponse;
import com.frameasy.dto.TradeDto;
import com.frameasy.security.UserPrincipal;
import com.frameasy.service.TradeService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.Collections;
import java.util.List;

/**
 * Trade (crops) listing API. When lat/lon are not provided, returns ALL approved trade.
 * When lat/lon provided, filters by radius (default 100 km).
 */
@RestController
@RequestMapping("/trade")
@RequiredArgsConstructor
public class TradeController {

    private static final double DEFAULT_RADIUS_KM = 100.0;

    private final TradeService tradeService;

    @GetMapping("/public")
    public ResponseEntity<ApiResponse<List<TradeDto>>> listPublic(
            @RequestParam(required = false) Double lat,
            @RequestParam(required = false) Double lon,
            @RequestParam(required = false) Double radiusKm,
            @RequestParam(required = false) String search) {
        double effectiveRadius = (radiusKm != null && radiusKm > 0) ? radiusKm : DEFAULT_RADIUS_KM;
        List<TradeDto> list = tradeService.listPublic(lat, lon, effectiveRadius, search);
        if (list == null) list = Collections.emptyList();
        return ResponseEntity.ok(ApiResponse.success(list));
    }

    @GetMapping("/my")
    public ResponseEntity<ApiResponse<List<TradeDto>>> myList(@AuthenticationPrincipal UserPrincipal principal) {
        if (principal == null) return ResponseEntity.status(401).build();
        return ResponseEntity.ok(ApiResponse.success(tradeService.listByUser(principal.getId())));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<TradeDto>> getById(@PathVariable Long id) {
        TradeDto dto = tradeService.getById(id);
        if (dto == null) return ResponseEntity.notFound().build();
        return ResponseEntity.ok(ApiResponse.success(dto));
    }

    @PostMapping
    public ResponseEntity<ApiResponse<TradeDto>> create(@AuthenticationPrincipal UserPrincipal principal,
                                                        @RequestBody TradeDto dto) {
        if (principal == null) return ResponseEntity.status(401).build();
        return ResponseEntity.ok(ApiResponse.success(tradeService.create(principal.getId(), dto)));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<TradeDto>> update(@AuthenticationPrincipal UserPrincipal principal,
                                                         @PathVariable Long id, @RequestBody TradeDto dto) {
        if (principal == null) return ResponseEntity.status(401).build();
        TradeDto updated = tradeService.update(id, principal.getId(), dto);
        if (updated == null) return ResponseEntity.notFound().build();
        return ResponseEntity.ok(ApiResponse.success(updated));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> delete(@AuthenticationPrincipal UserPrincipal principal, @PathVariable Long id) {
        if (principal == null) return ResponseEntity.status(401).build();
        boolean ok = tradeService.delete(id, principal.getId());
        if (!ok) return ResponseEntity.notFound().build();
        return ResponseEntity.ok(ApiResponse.success(null));
    }
}
