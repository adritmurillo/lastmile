package com.lastmile.infrastructure.adapter.in.rest;


import com.lastmile.application.usecase.dto.StatsDto;
import com.lastmile.domain.port.in.GetStatsUseCase;
import com.lastmile.infrastructure.adapter.in.rest.dto.response.ApiResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

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
}
