package com.sonarqube.adminapp.application.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProjectDTO {
    private String projectKey;
    private String projectName;
    private Long linesOfCode;
    private LocalDateTime lastScanDate;
    private LocalDateTime createdAt;
}

