package com.lastmile.application.usecase.dto;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class StatsDto {
    private long totalOrders;
    private long pendingOrders;
    private long assignedOrders;
    private long inTransitOrders;
    private long deliveredOrders;
    private long failedOrders;
    private long cancelledOrders;
    private double successRate;

    private long totalRoutes;
    private long pendingRoutes;
    private long inProgressRoutes;
    private long completedRoutes;

    private long totalCouriers;
    private long activeCouriers;
}