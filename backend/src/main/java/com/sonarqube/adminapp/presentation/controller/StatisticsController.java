package com.sonarqube.adminapp.presentation.controller;

import com.sonarqube.adminapp.application.dto.StatisticsDTO;
import com.sonarqube.adminapp.application.service.CleanupService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/statistics")
@RequiredArgsConstructor
@Tag(name = "Statistics", description = "Statistics management APIs")
public class StatisticsController {
    
    private final CleanupService cleanupService;
    
    @GetMapping
    @Operation(summary = "Get current statistics", description = "Returns total projects, LOC, and cleanup status")
    public ResponseEntity<StatisticsDTO> getStatistics() {
        StatisticsDTO statistics = cleanupService.getStatistics();
        return ResponseEntity.ok(statistics);
    }
}

