package com.sonarqube.adminapp.presentation.controller;

import com.sonarqube.adminapp.application.dto.ProjectDTO;
import com.sonarqube.adminapp.application.service.SonarQubeService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/projects")
@RequiredArgsConstructor
@Tag(name = "Projects", description = "Project management APIs")
public class ProjectController {
    
    private final SonarQubeService sonarQubeService;
    
    @GetMapping
    @Operation(summary = "Get all projects", description = "Returns list of all SonarQube projects")
    public ResponseEntity<List<ProjectDTO>> getAllProjects() {
        List<ProjectDTO> projects = sonarQubeService.getAllProjects();
        return ResponseEntity.ok(projects);
    }
    
    @PostMapping("/sync")
    @Operation(summary = "Sync projects", description = "Synchronizes projects from SonarQube")
    public ResponseEntity<Void> syncProjects() {
        sonarQubeService.syncProjects();
        return ResponseEntity.ok().build();
    }
}

