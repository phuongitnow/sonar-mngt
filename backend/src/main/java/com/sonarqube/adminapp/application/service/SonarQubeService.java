package com.sonarqube.adminapp.application.service;

import com.sonarqube.adminapp.application.dto.ProjectDTO;
import com.sonarqube.adminapp.domain.entity.ProjectSnapshot;

import java.util.List;

public interface SonarQubeService {
    List<ProjectDTO> getAllProjects();
    void deleteProject(String projectKey);
    Long getTotalLinesOfCode();
    void syncProjects();
}

