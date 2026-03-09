package com.lastmile.infrastructure.adapter.out.persistence;

import com.lastmile.domain.model.Order;
import com.lastmile.domain.model.OrderStatus;
import com.lastmile.domain.port.out.OrderRepository;
import com.lastmile.infrastructure.adapter.out.persistence.mapper.OrderPersistenceMapper;
import com.lastmile.infrastructure.adapter.out.persistence.repository.OrderJpaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class OrderRepositoryImpl implements OrderRepository {
    private final OrderJpaRepository jpaRepository;
    private final OrderPersistenceMapper mapper;

    @Override
    public Order save(Order order) {
        return mapper.toDomain(jpaRepository.save(mapper.toEntity(order)));
    }

    @Override
    public List<Order> saveAll(List<Order> orders) {
        return mapper.toDomainList(jpaRepository.saveAll(mapper.toEntityList(orders)));
    }

    @Override
    public Optional<Order> findById(UUID id) {
        return jpaRepository.findById(id).map(mapper :: toDomain);
    }

    @Override
    public Optional<Order> findByTrackingCode(String trackingCode) {
        return jpaRepository.findByTrackingCode(trackingCode).map(mapper :: toDomain);
    }

    @Override
    public Optional<Order> findByExternalTrackingCode(String externalTrackingCode) {
        return jpaRepository.findByExternalTrackingCode(externalTrackingCode).map(mapper :: toDomain);
    }

    @Override
    public List<Order> findByStatus(OrderStatus status) {
        return mapper.toDomainList(jpaRepository.findByStatus(status));
    }

    @Override
    public List<Order> findPendingForDate(LocalDate date) {
        return mapper.toDomainList(jpaRepository.findPendingOrdersForDate(date));
    }

    @Override
    public List<Order> findReschedulableFailedOrders() {
        return mapper.toDomainList(jpaRepository.findReschedulableFailedOrders());
    }

    @Override
    public boolean existsByExternalTrackingCode(String externalTrackingCode) {
        return jpaRepository.existsByExternalTrackingCode(externalTrackingCode);
    }

    @Override
    public List<Order> findAll() {
        return mapper.toDomainList(jpaRepository.findAll());
    }
}
