package com.lastmile.domain.port.in;

import com.lastmile.application.usecase.dto.StatsDto;

import java.time.LocalDate;

public interface GetStatsUseCase {
    StatsDto getTodayStats();
    StatsDto getStatsByPeriod(LocalDate startDate, LocalDate endDate);
}