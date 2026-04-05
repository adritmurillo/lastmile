package com.lastmile.infrastructure.adapter.out.persistence;

import com.lastmile.domain.model.OrderReturn;
import com.lastmile.domain.port.out.OrderReturnRepository;
import com.lastmile.infrastructure.adapter.out.persistence.entity.OrderReturnEntity;
import com.lastmile.infrastructure.adapter.out.persistence.mapper.OrderReturnPersistenceMapper;
import com.lastmile.infrastructure.adapter.out.persistence.repository.CourierJpaRepository;
import com.lastmile.infrastructure.adapter.out.persistence.repository.OrderJpaRepository;
import com.lastmile.infrastructure.adapter.out.persistence.repository.OrderReturnJpaRepository;
import com.lastmile.infrastructure.adapter.out.persistence.repository.RouteJpaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class OrderReturnRepositoryImpl implements OrderReturnRepository {
    
    private final OrderReturnJpaRepository jpaRepository;
    private final OrderReturnPersistenceMapper mapper;
    private final OrderJpaRepository orderJpaRepository;
    private final CourierJpaRepository courierJpaRepository;
    private final RouteJpaRepository routeJpaRepository;
    
    @Override
    @Transactional
    public OrderReturn save(OrderReturn orderReturn) {
        OrderReturnEntity entity = mapper.toEntity(orderReturn);
        
        // Set relationships
        if (orderReturn.getOrderId() != null) {
            orderJpaRepository.findById(orderReturn.getOrderId())
                    .ifPresent(entity::setOrder);
        }
        if (orderReturn.getCourierId() != null) {
            courierJpaRepository.findById(orderReturn.getCourierId())
                    .ifPresent(entity::setCourier);
        }
        if (orderReturn.getRouteId() != null) {
            routeJpaRepository.findById(orderReturn.getRouteId())
                    .ifPresent(entity::setRoute);
        }
        
        return mapper.toDomain(jpaRepository.save(entity));
    }
    
    @Override
    public boolean existsByOrderIdAndCourierIdAndReturnedAt(UUID orderId, UUID courierId, LocalDate date) {
        return !jpaRepository.findByOrderIdAndCourierIdAndDate(orderId, courierId, date).isEmpty();
    }
    
    @Override
    public List<UUID> findCourierIdsWhoReturnedOrderOnDate(UUID orderId, LocalDate date) {
        return jpaRepository.findCourierIdsByOrderIdAndDate(orderId, date);
    }
    
    @Override
    @Transactional
    public void saveAll(List<OrderReturn> orderReturns) {
        for (OrderReturn orderReturn : orderReturns) {
            save(orderReturn);
        }
    }
}
