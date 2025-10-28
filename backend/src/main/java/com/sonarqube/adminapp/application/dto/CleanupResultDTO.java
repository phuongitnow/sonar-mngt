package com.sonarqube.adminapp.application.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CleanupResultDTO {
    private Integer projectsDeleted;
    private Long linesOfCodeDeleted;
    private Long linesOfCodeRemaining;
    private String status;
    private String message;
}

