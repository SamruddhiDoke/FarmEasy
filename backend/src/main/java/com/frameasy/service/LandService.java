package com.frameasy.service;

import com.frameasy.dto.LandDto;
import com.frameasy.model.Land;
import com.frameasy.model.User;
import com.frameasy.repository.LandRepository;
import com.frameasy.repository.UserRepository;
import com.frameasy.util.GeoUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Land listing with safe location filtering.
 * - When user lat/lon are null: return all approved land (no distance filter).
 * - When land has null latitude/longitude: always include (never exclude).
 */
@Service
@RequiredArgsConstructor
public class LandService {

    private final LandRepository landRepository;
    private final UserRepository userRepository;

    private LandDto toDto(Land e, Double distanceKm) {
        LandDto dto = new LandDto();
        dto.setId(e.getId());
        dto.setUserId(e.getUserId());
        dto.setTitle(e.getTitle());
        dto.setDescription(e.getDescription());
        dto.setPricePerMonth(e.getPricePerMonth());
        dto.setImageUrl(e.getImageUrl());
        dto.setArea(e.getArea());
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

    @Transactional(readOnly = true)
    public List<LandDto> listPublic(Double userLat, Double userLon, Double radiusKm, String search) {
        List<Land> list = landRepository.findByIsApprovedTrueAndIsActiveTrue();
        if (search != null && !search.isBlank()) {
            list = list.stream()
                    .filter(e -> (e.getTitle() != null && e.getTitle().toLowerCase().contains(search.toLowerCase()))
                            || (e.getLocation() != null && e.getLocation().toLowerCase().contains(search.toLowerCase())))
                    .collect(Collectors.toList());
        }
        boolean filterByLocation = userLat != null && userLon != null && radiusKm != null && radiusKm > 0;

        List<LandDto> dtos = list.stream()
                .map(e -> {
                    if (e.getLatitude() == null || e.getLongitude() == null) {
                        return toDto(e, null);
                    }
                    double dist = filterByLocation
                            ? GeoUtil.distanceKm(userLat, userLon, e.getLatitude(), e.getLongitude())
                            : -1;
                    if (filterByLocation && dist >= 0 && dist > radiusKm) return null;
                    return toDto(e, dist >= 0 ? dist : null);
                })
                .filter(d -> d != null)
                .sorted(Comparator.comparing(LandDto::getDistanceKm, Comparator.nullsLast(Comparator.naturalOrder())))
                .collect(Collectors.toList());
        return dtos;
    }

    @Transactional(readOnly = true)
    public List<LandDto> listByUser(Long userId) {
        return landRepository.findByUserIdAndIsActiveTrue(userId).stream()
                .map(e -> toDto(e, null))
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public LandDto getById(Long id) {
        return landRepository.findById(id).map(e -> toDto(e, null)).orElse(null);
    }

    @Transactional
    public LandDto create(Long userId, LandDto dto) {
        Land e = new Land();
        e.setUserId(userId);
        e.setTitle(dto.getTitle());
        e.setDescription(dto.getDescription());
        e.setPricePerMonth(dto.getPricePerMonth());
        e.setImageUrl(dto.getImageUrl());
        e.setArea(dto.getArea());
        e.setLocation(dto.getLocation());
        e.setLatitude(dto.getLatitude());
        e.setLongitude(dto.getLongitude());
        if (e.getLatitude() == null || e.getLongitude() == null) {
            userRepository.findById(userId).ifPresent(owner -> {
                if (owner.getLatitude() != null && owner.getLongitude() != null) {
                    e.setLatitude(owner.getLatitude());
                    e.setLongitude(owner.getLongitude());
                }
            });
        }
        e.setIsApproved(false);
        e.setIsActive(true);
        Land saved = landRepository.save(e);
        return toDto(saved, null);
    }

    @Transactional
    public LandDto update(Long id, Long userId, LandDto dto) {
        Land e = landRepository.findById(id).orElse(null);
        if (e == null || !e.getUserId().equals(userId)) return null;
        if (dto.getTitle() != null) e.setTitle(dto.getTitle());
        if (dto.getDescription() != null) e.setDescription(dto.getDescription());
        if (dto.getPricePerMonth() != null) e.setPricePerMonth(dto.getPricePerMonth());
        if (dto.getImageUrl() != null) e.setImageUrl(dto.getImageUrl());
        if (dto.getArea() != null) e.setArea(dto.getArea());
        if (dto.getLocation() != null) e.setLocation(dto.getLocation());
        if (dto.getLatitude() != null) e.setLatitude(dto.getLatitude());
        if (dto.getLongitude() != null) e.setLongitude(dto.getLongitude());
        e = landRepository.save(e);
        return toDto(e, null);
    }

    @Transactional
    public boolean delete(Long id, Long userId) {
        Land e = landRepository.findById(id).orElse(null);
        if (e == null || !e.getUserId().equals(userId)) return false;
        e.setIsActive(false);
        landRepository.save(e);
        return true;
    }

    @Transactional
    public LandDto approve(Long id) {
        Land e = landRepository.findById(id).orElse(null);
        if (e == null) return null;
        e.setIsApproved(true);
        e = landRepository.save(e);
        return toDto(e, null);
    }
}
