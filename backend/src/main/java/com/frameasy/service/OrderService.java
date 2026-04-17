package com.frameasy.service;

import com.frameasy.dto.OrderDto;
import com.frameasy.dto.OrderRequest;
import com.frameasy.model.Order;
import com.frameasy.model.OrderItem;
import com.frameasy.repository.OrderRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class OrderService {

    private final OrderRepository orderRepository;

    private static OrderDto toDto(Order o) {
        OrderDto dto = new OrderDto();
        dto.setId(o.getId());
        dto.setUserId(o.getUserId());
        dto.setFullName(o.getFullName());
        dto.setAddress(o.getAddress());
        dto.setCity(o.getCity());
        dto.setState(o.getState());
        dto.setPincode(o.getPincode());
        dto.setPhone(o.getPhone());
        dto.setTotalAmount(o.getTotalAmount());
        dto.setStatus(o.getStatus());
        dto.setCreatedAt(o.getCreatedAt());
        dto.setItems(o.getItems().stream().map(i -> {
            OrderDto.Item it = new OrderDto.Item();
            it.setType(i.getType());
            it.setReferenceId(i.getReferenceId());
            it.setTitle(i.getTitle());
            it.setUnitPrice(i.getUnitPrice());
            it.setQty(i.getQty());
            return it;
        }).collect(Collectors.toList()));
        return dto;
    }

    @Transactional
    public OrderDto create(Long userId, OrderRequest req) {
        if (req == null || req.getAddress() == null || req.getItems() == null || req.getItems().isEmpty()) return null;

        BigDecimal total = req.getItems().stream()
                .map(i -> (i.getUnitPrice() == null ? BigDecimal.ZERO : i.getUnitPrice())
                        .multiply(BigDecimal.valueOf(i.getQty() == null ? 0 : i.getQty())))
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        Order order = new Order();
        order.setUserId(userId);
        order.setFullName(req.getAddress().getFullName());
        order.setAddress(req.getAddress().getAddress());
        order.setCity(req.getAddress().getCity());
        order.setState(req.getAddress().getState());
        order.setPincode(req.getAddress().getPincode());
        order.setPhone(req.getAddress().getPhone());
        order.setTotalAmount(total);
        order.setStatus("PLACED");

        List<OrderItem> items = req.getItems().stream().map(i -> OrderItem.builder()
                .type(i.getType())
                .referenceId(i.getReferenceId())
                .title(i.getTitle())
                .unitPrice(i.getUnitPrice())
                .qty(i.getQty())
                .build()).collect(Collectors.toList());
        order.setItems(items);

        return toDto(orderRepository.save(order));
    }

    @Transactional(readOnly = true)
    public List<OrderDto> listMy(Long userId) {
        return orderRepository.findByUserIdOrderByIdDesc(userId).stream().map(OrderService::toDto).toList();
    }

    @Transactional(readOnly = true)
    public List<OrderDto> listAll() {
        return orderRepository.findAll().stream().map(OrderService::toDto).toList();
    }
}

