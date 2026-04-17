package com.frameasy.controller;

import com.frameasy.dto.ApiResponse;
import com.frameasy.dto.OrderDto;
import com.frameasy.dto.OrderRequest;
import com.frameasy.security.UserPrincipal;
import com.frameasy.service.OrderService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/orders")
@RequiredArgsConstructor
public class OrderController {

    private final OrderService orderService;

    @PostMapping
    public ResponseEntity<ApiResponse<OrderDto>> create(@AuthenticationPrincipal UserPrincipal principal,
                                                        @RequestBody OrderRequest request) {
        if (principal == null) return ResponseEntity.status(401).build();
        OrderDto created = orderService.create(principal.getId(), request);
        if (created == null) return ResponseEntity.badRequest().body(ApiResponse.error("Invalid order payload"));
        return ResponseEntity.ok(ApiResponse.success(created));
    }

    @GetMapping("/my")
    public ResponseEntity<ApiResponse<List<OrderDto>>> my(@AuthenticationPrincipal UserPrincipal principal) {
        if (principal == null) return ResponseEntity.status(401).build();
        return ResponseEntity.ok(ApiResponse.success(orderService.listMy(principal.getId())));
    }

    @GetMapping("/admin/all")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<List<OrderDto>>> all() {
        return ResponseEntity.ok(ApiResponse.success(orderService.listAll()));
    }
}

