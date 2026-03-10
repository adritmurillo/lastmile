package com.lastmile.application.usecase;

import com.lastmile.application.usecase.dto.StatsDto;
import com.lastmile.domain.model.OrderStatus;
import com.lastmile.domain.model.RouteStatus;
import com.lastmile.domain.port.in.GetStatsUseCase;
import com.lastmile.domain.port.out.CourierRepository;
import com.lastmile.domain.port.out.OrderRepository;
import com.lastmile.domain.port.out.RouteRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class GetStatsUseCaseImpl implements GetStatsUseCase {

    private final OrderRepository orderRepository;
    private final RouteRepository routeRepository;
    private final CourierRepository courierRepository;

    @Override
    public StatsDto getTodayStats() {
        log.info("Fetching today's stats for date: {}", LocalDate.now());

        List<com.lastmile.domain.model.Order> allOrders = orderRepository.findAll();
        List<com.lastmile.domain.model.Route> todayRoutes = routeRepository.findByDate(LocalDate.now());
        List<com.lastmile.domain.model.Courier> allCouriers = courierRepository.findAll();

        long pending   = allOrders.stream().filter(o -> o.getStatus() == OrderStatus.PENDING).count();
        long assigned  = allOrders.stream().filter(o -> o.getStatus() == OrderStatus.ASSIGNED).count();
        long inTransit = allOrders.stream().filter(o -> o.getStatus() == OrderStatus.IN_TRANSIT).count();
        long delivered = allOrders.stream().filter(o -> o.getStatus() == OrderStatus.DELIVERED).count();
        long failed    = allOrders.stream().filter(o -> o.getStatus() == OrderStatus.FAILED).count();
        long cancelled = allOrders.stream().filter(o -> o.getStatus() == OrderStatus.CANCELLED).count();

        long totalOrders = allOrders.size();
        double successRate = totalOrders > 0
                ? Math.round((delivered * 100.0 / totalOrders) * 10.0) / 10.0
                : 0.0;

        List<com.lastmile.domain.model.Route> activeRoutes = todayRoutes.stream()
                .filter(r -> r.getStatus() != RouteStatus.CANCELLED)
                .toList();

        long pendingRoutes    = activeRoutes.stream().filter(r -> r.getStatus() == RouteStatus.PENDING).count();
        long inProgressRoutes = activeRoutes.stream().filter(r -> r.getStatus() == RouteStatus.IN_PROGRESS).count();
        long completedRoutes  = activeRoutes.stream().filter(r -> r.getStatus() == RouteStatus.COMPLETED).count();

        long activeCouriers = allCouriers.stream()
                .filter(c -> c.getStatus() == com.lastmile.domain.model.CourierStatus.ACTIVE)
                .count();

        return StatsDto.builder()
                .totalOrders(totalOrders)
                .pendingOrders(pending)
                .assignedOrders(assigned)
                .inTransitOrders(inTransit)
                .deliveredOrders(delivered)
                .failedOrders(failed)
                .cancelledOrders(cancelled)
                .successRate(successRate)
                .totalRoutes(activeRoutes.size())
                .pendingRoutes(pendingRoutes)
                .inProgressRoutes(inProgressRoutes)
                .completedRoutes(completedRoutes)
                .totalCouriers(allCouriers.size())
                .activeCouriers(activeCouriers)
                .build();
    }
}