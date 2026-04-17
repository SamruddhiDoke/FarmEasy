package com.frameasy.controller;

import com.frameasy.dto.*;
import com.frameasy.model.User;
import com.frameasy.repository.*;
import com.frameasy.service.EquipmentService;
import com.frameasy.service.LandService;
import com.frameasy.service.SchemeService;
import com.frameasy.service.TradeService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/admin")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
public class AdminController {

    private final UserRepository userRepository;
    private final EquipmentRepository equipmentRepository;
    private final LandRepository landRepository;
    private final TradeRepository tradeRepository;
    private final AgreementRepository agreementRepository;
    private final com.frameasy.repository.OrderRepository orderRepository;
    private final SchemeService schemeService;
    private final EquipmentService equipmentService;
    private final LandService landService;
    private final TradeService tradeService;

    // Avoid circular ref: use constructor injection only for repos and schemeService; for approve use direct repo update
    @GetMapping("/stats")
    public ResponseEntity<ApiResponse<Map<String, Object>>> stats() {
        Map<String, Object> m = new HashMap<>();
        m.put("users", userRepository.count());
        m.put("equipment", equipmentRepository.count());
        m.put("land", landRepository.count());
        m.put("trade", tradeRepository.count());
        m.put("agreements", agreementRepository.count());
        m.put("orders", orderRepository.count());
        m.put("schemes", schemeService.listAll().size());
        return ResponseEntity.ok(ApiResponse.success(m));
    }

    @GetMapping("/users")
    public ResponseEntity<ApiResponse<List<User>>> listUsers() {
        return ResponseEntity.ok(ApiResponse.success(userRepository.findByIsActiveTrue()));
    }

    @GetMapping("/equipment/pending")
    public ResponseEntity<ApiResponse<List<EquipmentDto>>> pendingEquipment() {
        List<EquipmentDto> list = equipmentRepository.findAll().stream()
                .filter(e -> !e.getIsApproved() && e.getIsActive())
                .map(e -> equipmentService.getById(e.getId()))
                .filter(d -> d != null)
                .collect(Collectors.toList());
        return ResponseEntity.ok(ApiResponse.success(list));
    }

    @PostMapping("/equipment/{id}/approve")
    public ResponseEntity<ApiResponse<EquipmentDto>> approveEquipment(@PathVariable Long id) {
        EquipmentDto dto = equipmentService.approve(id);
        if (dto == null) return ResponseEntity.notFound().build();
        return ResponseEntity.ok(ApiResponse.success(dto));
    }

    @DeleteMapping("/equipment/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteEquipment(@PathVariable Long id) {
        equipmentRepository.findById(id).ifPresent(e -> {
            e.setIsActive(false);
            equipmentRepository.save(e);
        });
        return ResponseEntity.ok(ApiResponse.success(null));
    }

    @GetMapping("/land/pending")
    public ResponseEntity<ApiResponse<List<LandDto>>> pendingLand() {
        List<LandDto> list = landRepository.findAll().stream()
                .filter(e -> !e.getIsApproved() && e.getIsActive())
                .map(e -> landService.getById(e.getId()))
                .filter(d -> d != null)
                .collect(Collectors.toList());
        return ResponseEntity.ok(ApiResponse.success(list));
    }

    @PostMapping("/land/{id}/approve")
    public ResponseEntity<ApiResponse<LandDto>> approveLand(@PathVariable Long id) {
        LandDto dto = landService.approve(id);
        if (dto == null) return ResponseEntity.notFound().build();
        return ResponseEntity.ok(ApiResponse.success(dto));
    }

    @GetMapping("/trade/pending")
    public ResponseEntity<ApiResponse<List<TradeDto>>> pendingTrade() {
        List<TradeDto> list = tradeRepository.findAll().stream()
                .filter(e -> !e.getIsApproved() && e.getIsActive())
                .map(e -> tradeService.getById(e.getId()))
                .filter(d -> d != null)
                .collect(Collectors.toList());
        return ResponseEntity.ok(ApiResponse.success(list));
    }

    @PostMapping("/trade/{id}/approve")
    public ResponseEntity<ApiResponse<TradeDto>> approveTrade(@PathVariable Long id) {
        TradeDto dto = tradeService.approve(id);
        if (dto == null) return ResponseEntity.notFound().build();
        return ResponseEntity.ok(ApiResponse.success(dto));
    }

    @DeleteMapping("/trade/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteTrade(@PathVariable Long id) {
        tradeRepository.findById(id).ifPresent(e -> {
            e.setIsActive(false);
            tradeRepository.save(e);
        });
        return ResponseEntity.ok(ApiResponse.success(null));
    }

    @DeleteMapping("/land/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteLand(@PathVariable Long id) {
        landRepository.findById(id).ifPresent(e -> {
            e.setIsActive(false);
            landRepository.save(e);
        });
        return ResponseEntity.ok(ApiResponse.success(null));
    }

    @GetMapping("/agreements")
    public ResponseEntity<ApiResponse<List<AgreementDto>>> listAgreements() {
        List<AgreementDto> list = agreementRepository.findAll().stream()
                .map(a -> {
                    AgreementDto d = new AgreementDto();
                    d.setId(a.getId());
                    d.setAgreementType(a.getAgreementType());
                    d.setReferenceId(a.getReferenceId());
                    d.setSellerId(a.getSellerId());
                    d.setBuyerId(a.getBuyerId());
                    d.setBuyerName(a.getBuyerName());
                    d.setFinalPrice(a.getFinalPrice());
                    d.setDueDate(a.getDueDate());
                    d.setPdfPath(a.getPdfPath());
                    d.setSignedAt(a.getSignedAt());
                    return d;
                })
                .collect(Collectors.toList());
        return ResponseEntity.ok(ApiResponse.success(list));
    }

    @PostMapping("/schemes/refresh")
    public ResponseEntity<ApiResponse<String>> refreshSchemes() {
        int count = schemeService.fetchAndCache();
        return ResponseEntity.ok(ApiResponse.success("Schemes refreshed: " + count + " entries"));
    }
}
