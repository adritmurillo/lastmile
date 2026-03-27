package com.lastmile.infrastructure.adapter.out.persistence;

import com.lastmile.domain.model.CloseRequestStatus;
import com.lastmile.domain.model.RouteCloseRequest;
import com.lastmile.domain.port.out.RouteCloseRequestRepository;
import com.lastmile.infrastructure.adapter.out.persistence.entity.RouteCloseRequestEntity;
import com.lastmile.infrastructure.adapter.out.persistence.mapper.RouteCloseRequestPersistenceMapper;
import com.lastmile.infrastructure.adapter.out.persistence.repository.CourierJpaRepository;
import com.lastmile.infrastructure.adapter.out.persistence.repository.RouteCloseRequestJpaRepository;
import com.lastmile.infrastructure.adapter.out.persistence.repository.RouteJpaRepository;
import com.lastmile.infrastructure.adapter.out.persistence.repository.UserJpaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class RouteCloseRequestRepositoryImpl implements RouteCloseRequestRepository {
    
    private final RouteCloseRequestJpaRepository jpaRepository;
    private final RouteCloseRequestPersistenceMapper mapper;
    private final RouteJpaRepository routeJpaRepository;
    private final CourierJpaRepository courierJpaRepository;
    private final UserJpaRepository userJpaRepository;
    
    @Override
    @Transactional
    public RouteCloseRequest save(RouteCloseRequest request) {
        RouteCloseRequestEntity entity = mapper.toEntity(request);
        
        // Set relationships
        if (request.getRouteId() != null) {
            routeJpaRepository.findById(request.getRouteId())
                    .ifPresent(entity::setRoute);
        }
        if (request.getCourierId() != null) {
            courierJpaRepository.findById(request.getCourierId())
                    .ifPresent(entity::setCourier);
        }
        if (request.getReviewedBy() != null) {
            userJpaRepository.findById(request.getReviewedBy())
                    .ifPresent(entity::setReviewedBy);
        }
        
        return mapper.toDomain(jpaRepository.save(entity));
    }
    
    @Override
    public Optional<RouteCloseRequest> findById(UUID id) {
        return jpaRepository.findById(id).map(mapper::toDomain);
    }
    
    @Override
    public Optional<RouteCloseRequest> findByRouteId(UUID routeId) {
        List<RouteCloseRequestEntity> requests = jpaRepository.findByRouteId(routeId);
        return requests.isEmpty() ? Optional.empty() : Optional.of(mapper.toDomain(requests.get(0)));
    }
    
    @Override
    public List<RouteCloseRequest> findByStatus(CloseRequestStatus status) {
        return jpaRepository.findByStatusWithDetails(status.name())
                .stream()
                .map(mapper::toDomain)
                .toList();
    }
    
    @Override
    public List<RouteCloseRequest> findByCourierIdAndCreatedAtAfter(UUID courierId, LocalDateTime since) {
        return jpaRepository.findByCourierIdAndStatusSince(courierId, CloseRequestStatus.APPROVED.name(), since)
                .stream()
                .map(mapper::toDomain)
                .toList();
    }
    
    @Override
    public long countByCourierIdAndStatusAndCreatedAtAfter(UUID courierId, CloseRequestStatus status, LocalDateTime since) {
        return jpaRepository.countApprovedByCourierIdSince(courierId, since);
    }
}
