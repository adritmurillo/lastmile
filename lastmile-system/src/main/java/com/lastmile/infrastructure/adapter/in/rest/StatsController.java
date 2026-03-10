package com.lastmile.infrastructure.adapter.in.rest;


import com.lastmile.application.usecase.dto.StatsDto;
import com.lastmile.domain.port.in.GetStatsUseCase;
import com.lastmile.infrastructure.adapter.in.rest.dto.response.ApiResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;

@Slf4j
@RestController
@RequestMapping("/api/v1/stats")
@RequiredArgsConstructor
public class StatsController {
    private final GetStatsUseCase getStatsUseCase;

    @GetMapping("/today")
    @PreAuthorize("hasAnyRole('ADMIN', 'DISPATCHER')")
    public ResponseEntity<ApiResponse<StatsDto>> getTodayStats() {
        log.info("GET /api/v1/stats/today");
        StatsDto stats = getStatsUseCase.getTodayStats();
        return ResponseEntity.ok(ApiResponse.ok(stats));
    }

    @GetMapping("/period")
    @PreAuthorize("hasAnyRole('ADMIN', 'DISPATCHER')")
    public ResponseEntity<ApiResponse<StatsDto>> getStatsByPeriod(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {
        log.info("GET /api/v1/stats/period?startDate={}&endDate={}", startDate, endDate);
        StatsDto stats = getStatsUseCase.getStatsByPeriod(startDate, endDate);
        return ResponseEntity.ok(ApiResponse.ok(stats));
    }
}
