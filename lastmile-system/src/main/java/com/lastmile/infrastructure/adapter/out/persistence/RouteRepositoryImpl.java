package com.lastmile.infrastructure.adapter.out.persistence;


import com.lastmile.domain.model.Route;
import com.lastmile.domain.model.Stop;
import com.lastmile.domain.port.out.RouteRepository;
import com.lastmile.infrastructure.adapter.out.persistence.entity.RouteEntity;
import com.lastmile.infrastructure.adapter.out.persistence.entity.StopEntity;
import com.lastmile.infrastructure.adapter.out.persistence.entity.StopPhotoEntity;
import com.lastmile.infrastructure.adapter.out.persistence.mapper.RoutePersistenceMapper;
import com.lastmile.infrastructure.adapter.out.persistence.mapper.StopPersistenceMapper;
import com.lastmile.infrastructure.adapter.out.persistence.repository.CourierJpaRepository;
import com.lastmile.infrastructure.adapter.out.persistence.repository.RouteJpaRepository;
import com.lastmile.infrastructure.adapter.out.persistence.repository.StopJpaRepository;
import com.lastmile.infrastructure.adapter.out.persistence.repository.StopPhotoJpaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class RouteRepositoryImpl implements RouteRepository {

    private final RouteJpaRepository routeJpaRepository;
    private final StopJpaRepository stopJpaRepository;
    private final RoutePersistenceMapper routeMapper;
    private final StopPersistenceMapper stopMapper;
    private final StopPhotoJpaRepository stopPhotoJpaRepository;
    private final CourierJpaRepository courierJpaRepository;


    @Override
    public List<Route> findCompletedByCourier(UUID courierId) {
        return routeMapper.toDomainList(
                routeJpaRepository.findCompletedByCourier(courierId, LocalDate.now())
        );
    }

    @Override
    public Route save(Route route) {
        RouteEntity routeEntity = routeMapper.toEntity(route);
        if (route.getCourier() != null) {
            courierJpaRepository.findById(route.getCourier().getId()).ifPresent(
                    routeEntity::setCourier
            );
        }

        if (routeEntity.getStops() != null) {
            routeEntity.getStops().forEach(stop -> stop.setRoute(routeEntity));
        }

        return routeMapper.toDomain(routeJpaRepository.save(routeEntity));
    }

    @Override
    public Optional<Route> findById(UUID id) {
        return routeJpaRepository.findById(id).map(routeMapper :: toDomain);
    }

    @Override
    public Optional<Route> findActiveCourierRoute(UUID courierId, LocalDate date) {
        return routeJpaRepository.findActiveCourierRoute(courierId, date).map(routeMapper :: toDomain);
    }

    @Override
    public List<Route> findByDate(LocalDate date) {
        return routeMapper.toDomainList(
                routeJpaRepository.findByDateWithDetails(date)
                        .stream()
                        .filter(r -> r.getStatus() != com.lastmile.domain.model.RouteStatus.CANCELLED)
                        .toList()
        );
    }

    @Override
    public Stop saveStop(Stop stop) {
        StopEntity entity = stopMapper.toEntity(stop);
        if (stop.getRouteId() != null) {
            routeJpaRepository.findById(stop.getRouteId())
                    .ifPresent(entity::setRoute);
        }
        return stopMapper.toDomain(stopJpaRepository.save(entity));
    }

    @Override
    public Optional<Stop> findStopById(UUID id) {
        return stopJpaRepository.findByIdWithDetails(id).map(stopMapper :: toDomain);
    }

    @Override
    public Optional<Route> findRouteByOrderId(UUID orderId) {
        return routeJpaRepository.findByOrderId(orderId).stream().findFirst().map(routeMapper :: toDomain);
    }

    @Override
    public List<Route> findByDateRange(LocalDate startDate, LocalDate endDate) {
        return routeMapper.toDomainList(
                routeJpaRepository.findByDateRangeWithDetails(startDate, endDate));
    }

    @Override
    public List<Stop> findPendingStopsByCourier(UUID courierId) {
        return stopJpaRepository
                .findPendingByCourierBeforeDate(courierId, LocalDate.now())
                .stream()
                .map(stopMapper :: toDomain)
                .toList();
    }

    @Override
    public List<Route> findRoutesByOrderId(UUID orderId) {
        return routeMapper.toDomainList(routeJpaRepository.findByOrderId(orderId));
    }

    @Override
    public void saveStopPhotos(UUID stopId, List<String> photoUrls) {
        StopEntity stopEntity = stopJpaRepository.findById(stopId)
                .orElseThrow(() -> new RuntimeException("Stop not found: " +stopId));
        for (int i = 0; i < photoUrls.size(); i++) {
            StopPhotoEntity photo = StopPhotoEntity.builder()
                    .id(UUID.randomUUID())
                    .stop(stopEntity)
                    .photoUrl(photoUrls.get(i))
                    .takenAt(LocalDateTime.now())
                    .photoOrder(i + 1)
                    .build();
            stopPhotoJpaRepository.save(photo);
        }
    }

    @Override
    public List<String> getStopPhotos(UUID stopId) {
        return stopPhotoJpaRepository.findByStopIdOrderByPhotoOrderAsc(stopId)
                .stream()
                .map(StopPhotoEntity :: getPhotoUrl)
                .toList();
    }
}