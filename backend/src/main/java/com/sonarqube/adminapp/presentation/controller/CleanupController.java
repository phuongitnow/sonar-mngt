package com.sonarqube.adminapp.presentation.controller;

import com.sonarqube.adminapp.application.dto.CleanupResultDTO;
import com.sonarqube.adminapp.application.service.CleanupService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/cleanup")
@RequiredArgsConstructor
@Tag(name = "Cleanup", description = "Cleanup operation APIs")
public class CleanupController {
    
    private final CleanupService cleanupService;
    
    @PostMapping("/execute")
    @Operation(summary = "Execute cleanup manually", description = "Triggers the cleanup process manually")
    public ResponseEntity<CleanupResultDTO> executeCleanup() {
        CleanupResultDTO result = cleanupService.performCleanup();
        return ResponseEntity.ok(result);
    }
}

