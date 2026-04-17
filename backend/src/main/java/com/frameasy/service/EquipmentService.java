package com.frameasy.service;

import com.frameasy.dto.EquipmentDto;
import com.frameasy.model.Equipment;
import com.frameasy.model.User;
import com.frameasy.repository.EquipmentRepository;
import com.frameasy.repository.UserRepository;
import com.frameasy.util.GeoUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Equipment business logic with safe location filtering.
 * - When user lat/lon are null: return ALL approved equipment (no distance filter).
 * - When equipment has null latitude/longitude: always INCLUDE it (never exclude due to missing coords).
 * - Distance filter only excludes when both user and equipment have valid coordinates and distance > radius.
 */
@Service
@RequiredArgsConstructor
public class EquipmentService {

    private final EquipmentRepository equipmentRepository;
    private final UserRepository userRepository;

    private EquipmentDto toDto(Equipment e, Double distanceKm) {
        EquipmentDto dto = new EquipmentDto();
        dto.setId(e.getId());
        dto.setUserId(e.getUserId());
        dto.setTitle(e.getTitle());
        dto.setDescription(e.getDescription());
        dto.setPricePerDay(e.getPricePerDay());
        dto.setImageUrl(e.getImageUrl());
        dto.setCategory(e.getCategory());
        dto.setAvailability(e.getAvailability());
        dto.setLocation(e.getLocation());
        dto.setLatitude(e.getLatitude());
        dto.setLongitude(e.getLongitude());
        dto.setIsApproved(e.getIsApproved());
        dto.setIsActive(e.getIsActive());
        dto.setCreatedAt(e.getCreatedAt());
        dto.setDistanceKm(distanceKm);
        userRepository.findById(e.getUserId()).ifPresent(u -> dto.setOwnerName(u.getName()));
        return dto;
    }

    /**
     * List public equipment with safe filtering.
     * If userLat or userLon is null → no location filter, return all (after category/price filters).
     * Equipment with null lat/lon is always included (location-independent listing).
     */
    @Transactional(readOnly = true)
    public List<EquipmentDto> listPublic(String category, BigDecimal minPrice, BigDecimal maxPrice,
                                         Double userLat, Double userLon, Double radiusKm) {
        // Demo-friendly: show all ACTIVE equipment, ignore approval flag
        List<Equipment> list = equipmentRepository.findByIsActiveTrue();

        if (category != null && !category.isBlank()) {
            list = list.stream().filter(e -> category.equals(e.getCategory())).collect(Collectors.toList());
        }
        if (minPrice != null) {
            list = list.stream().filter(e -> e.getPricePerDay().compareTo(minPrice) >= 0).collect(Collectors.toList());
        }
        if (maxPrice != null) {
            list = list.stream().filter(e -> e.getPricePerDay().compareTo(maxPrice) <= 0).collect(Collectors.toList());
        }

        boolean filterByLocation = userLat != null && userLon != null && radiusKm != null && radiusKm > 0;

        List<EquipmentDto> dtos = list.stream()
                .map(e -> {
                    // Safe: equipment with null coordinates is always included (never excluded)
                    if (e.getLatitude() == null || e.getLongitude() == null) {
                        return toDto(e, null);
                    }
                    double dist = filterByLocation
                            ? GeoUtil.distanceKm(userLat, userLon, e.getLatitude(), e.getLongitude())
                            : -1;
                    if (filterByLocation && dist >= 0 && dist > radiusKm) {
                        return null;
                    }
                    return toDto(e, dist >= 0 ? dist : null);
                })
                .filter(d -> d != null)
                .sorted(Comparator.comparing(EquipmentDto::getDistanceKm, Comparator.nullsLast(Comparator.naturalOrder())))
                .collect(Collectors.toList());
        return dtos;
    }

    @Transactional(readOnly = true)
    public List<EquipmentDto> listByUser(Long userId) {
        return equipmentRepository.findByUserIdAndIsActiveTrue(userId).stream()
                .map(e -> toDto(e, null))
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public EquipmentDto getById(Long id) {
        return equipmentRepository.findById(id)
                .map(e -> toDto(e, null))
                .orElse(null);
    }

    /**
     * Create equipment. If dto has no lat/lng, assign owner's lat/lng when available.
     * If owner has null coords, equipment is created without coords (location-independent).
     */
  @Transactional
public EquipmentDto create(Long userId, EquipmentDto dto) {

    Equipment e = new Equipment();
    e.setUserId(userId);
    e.setTitle(dto.getTitle());
    e.setDescription(dto.getDescription());
    e.setPricePerDay(dto.getPricePerDay());
    e.setImageUrl(dto.getImageUrl());
    e.setCategory(dto.getCategory());
    e.setAvailability(dto.getAvailability());
    e.setLocation(dto.getLocation());
    e.setLatitude(dto.getLatitude());
    e.setLongitude(dto.getLongitude());

    // If equipment has no coordinates, try assigning from owner
    if (e.getLatitude() == null || e.getLongitude() == null) {
        Optional<User> ownerOpt = userRepository.findById(userId);

        if (ownerOpt.isPresent()) {
            User owner = ownerOpt.get();

            if (owner.getLatitude() != null && owner.getLongitude() != null) {
                e.setLatitude(owner.getLatitude());
                e.setLongitude(owner.getLongitude());
            }
        }
    }

    e.setIsApproved(false);
    e.setIsActive(true);

    Equipment saved = equipmentRepository.save(e);

    return toDto(saved, null);
}


    @Transactional
    public EquipmentDto update(Long id, Long userId, EquipmentDto dto) {
        Equipment e = equipmentRepository.findById(id).orElse(null);
        if (e == null || !e.getUserId().equals(userId)) return null;
        if (dto.getTitle() != null) e.setTitle(dto.getTitle());
        if (dto.getDescription() != null) e.setDescription(dto.getDescription());
        if (dto.getPricePerDay() != null) e.setPricePerDay(dto.getPricePerDay());
        if (dto.getImageUrl() != null) e.setImageUrl(dto.getImageUrl());
        if (dto.getCategory() != null) e.setCategory(dto.getCategory());
        if (dto.getAvailability() != null) e.setAvailability(dto.getAvailability());
        if (dto.getLocation() != null) e.setLocation(dto.getLocation());
        if (dto.getLatitude() != null) e.setLatitude(dto.getLatitude());
        if (dto.getLongitude() != null) e.setLongitude(dto.getLongitude());
        e = equipmentRepository.save(e);
        return toDto(e, null);
    }

    @Transactional
    public boolean delete(Long id, Long userId) {
        Equipment e = equipmentRepository.findById(id).orElse(null);
        if (e == null || !e.getUserId().equals(userId)) return false;
        e.setIsActive(false);
        equipmentRepository.save(e);
        return true;
    }

    @Transactional
    public EquipmentDto approve(Long id) {
        Equipment e = equipmentRepository.findById(id).orElse(null);
        if (e == null) return null;
        e.setIsApproved(true);
        e = equipmentRepository.save(e);
        return toDto(e, null);
    }
}
