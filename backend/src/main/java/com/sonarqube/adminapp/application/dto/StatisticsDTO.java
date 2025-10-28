package com.sonarqube.adminapp.application.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class StatisticsDTO {
    private Integer totalProjects;
    private Long totalLinesOfCode;
    private Long maxLinesOfCode;
    private Double percentageUsage;
    private Integer projectsToDelete;
}

