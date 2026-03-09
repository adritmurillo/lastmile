package com.lastmile.domain.port.in;

import com.lastmile.application.usecase.dto.StatsDto;

public interface GetStatsUseCase {
    StatsDto getTodayStats();
}