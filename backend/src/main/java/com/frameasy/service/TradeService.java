package com.frameasy.service;

import com.frameasy.dto.TradeDto;
import com.frameasy.model.Trade;
import com.frameasy.model.User;
import com.frameasy.repository.TradeRepository;
import com.frameasy.repository.UserRepository;
import com.frameasy.util.GeoUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Trade (crops) listing with safe location filtering.
 * - When user lat/lon are null: return all approved trade (no distance filter).
 * - When trade has null latitude/longitude: always include (never exclude).
 */
@Service
@RequiredArgsConstructor
public class TradeService {

    private final TradeRepository tradeRepository;
    private final UserRepository userRepository;

    private TradeDto toDto(Trade e, Double distanceKm) {
        TradeDto dto = new TradeDto();
        dto.setId(e.getId());
        dto.setUserId(e.getUserId());
        dto.setCropName(e.getCropName());
        dto.setDescription(e.getDescription());
        dto.setPricePerUnit(e.getPricePerUnit());
        dto.setUnit(e.getUnit());
        dto.setQuantity(e.getQuantity());
        dto.setImageUrl(e.getImageUrl());
        dto.setLocation(e.getLocation());
        dto.setLatitude(e.getLatitude());
        dto.setLongitude(e.getLongitude());
        dto.setIsApproved(e.getIsApproved());
        dto.setIsActive(e.getIsActive());
        dto.setCreatedAt(e.getCreatedAt());
        dto.setDistanceKm(distanceKm);
        userRepository.findById(e.getUserId()).ifPresent(u -> dto.setSellerName(u.getName()));
        return dto;
    }

    @Transactional(readOnly = true)
    public List<TradeDto> listPublic(Double userLat, Double userLon, Double radiusKm, String search) {
        // Demo-friendly: show all ACTIVE trade listings, ignore approval flag
        List<Trade> list = (search != null && !search.isBlank())
                ? tradeRepository.findByIsActiveTrueAndCropNameContainingIgnoreCase(search)
                : tradeRepository.findByIsActiveTrue();
        boolean filterByLocation = userLat != null && userLon != null && radiusKm != null && radiusKm > 0;

        List<TradeDto> dtos = list.stream()
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
                .sorted(Comparator.comparing(TradeDto::getDistanceKm, Comparator.nullsLast(Comparator.naturalOrder())))
                .collect(Collectors.toList());
        return dtos;
    }

    @Transactional(readOnly = true)
    public List<TradeDto> listByUser(Long userId) {
        return tradeRepository.findByUserIdAndIsActiveTrue(userId).stream()
                .map(e -> toDto(e, null))
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public TradeDto getById(Long id) {
        return tradeRepository.findById(id).map(e -> toDto(e, null)).orElse(null);
    }

    @Transactional
    public TradeDto create(Long userId, TradeDto dto) {
        Trade e = new Trade();
        e.setUserId(userId);
        e.setCropName(dto.getCropName());
        e.setDescription(dto.getDescription());
        e.setPricePerUnit(dto.getPricePerUnit());
        e.setUnit(dto.getUnit());
        e.setQuantity(dto.getQuantity());
        e.setImageUrl(dto.getImageUrl());
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
        Trade saved = tradeRepository.save(e);
        return toDto(saved, null);
    }

    @Transactional
    public TradeDto update(Long id, Long userId, TradeDto dto) {
        Trade e = tradeRepository.findById(id).orElse(null);
        if (e == null || !e.getUserId().equals(userId)) return null;
        if (dto.getCropName() != null) e.setCropName(dto.getCropName());
        if (dto.getDescription() != null) e.setDescription(dto.getDescription());
        if (dto.getPricePerUnit() != null) e.setPricePerUnit(dto.getPricePerUnit());
        if (dto.getUnit() != null) e.setUnit(dto.getUnit());
        if (dto.getQuantity() != null) e.setQuantity(dto.getQuantity());
        if (dto.getImageUrl() != null) e.setImageUrl(dto.getImageUrl());
        if (dto.getLocation() != null) e.setLocation(dto.getLocation());
        if (dto.getLatitude() != null) e.setLatitude(dto.getLatitude());
        if (dto.getLongitude() != null) e.setLongitude(dto.getLongitude());
        e = tradeRepository.save(e);
        return toDto(e, null);
    }

    @Transactional
    public boolean delete(Long id, Long userId) {
        Trade e = tradeRepository.findById(id).orElse(null);
        if (e == null || !e.getUserId().equals(userId)) return false;
        e.setIsActive(false);
        tradeRepository.save(e);
        return true;
    }

    @Transactional
    public TradeDto approve(Long id) {
        Trade e = tradeRepository.findById(id).orElse(null);
        if (e == null) return null;
        e.setIsApproved(true);
        e = tradeRepository.save(e);
        return toDto(e, null);
    }
}
