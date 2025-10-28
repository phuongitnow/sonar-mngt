package com.sonarqube.adminapp.infrastructure.sonarqube;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.sonarqube.adminapp.application.dto.ProjectDTO;
import com.sonarqube.adminapp.application.service.SonarQubeService;
import com.sonarqube.adminapp.domain.entity.ProjectSnapshot;
import com.sonarqube.adminapp.domain.repository.ProjectSnapshotRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import okhttp3.*;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class SonarQubeServiceImpl implements SonarQubeService {
    
    private final ProjectSnapshotRepository projectSnapshotRepository;
    private final ObjectMapper objectMapper;
    
    @Value("${sonarqube.url}")
    private String sonarQubeUrl;
    
    @Value("${sonarqube.username}")
    private String username;
    
    @Value("${sonarqube.password}")
    private String password;
    
    @Value("${sonarqube.token}")
    private String token;
    
    private final OkHttpClient httpClient = new OkHttpClient();
    
    @Override
    public List<ProjectDTO> getAllProjects() {
        List<ProjectDTO> projects = new ArrayList<>();
        int page = 1;
        int pageSize = 500;
        
        try {
            while (true) {
                String url = String.format("%s/api/projects/search?p=%d&ps=%d", sonarQubeUrl, page, pageSize);
                Request request = buildRequest(url);
                
                try (Response response = httpClient.newCall(request).execute()) {
                    if (!response.isSuccessful()) {
                        log.error("Failed to get projects from SonarQube: {}", response.message());
                        break;
                    }
                    
                    String responseBody = response.body().string();
                    JsonNode jsonResponse = objectMapper.readTree(responseBody);
                    JsonNode components = jsonResponse.get("components");
                    
                    if (components == null || components.isEmpty()) {
                        break;
                    }
                    
                    for (JsonNode component : components) {
                        ProjectDTO project = ProjectDTO.builder()
                                .projectKey(component.get("key").asText())
                                .projectName(component.get("name").asText())
                                .build();
                        
                        Long linesOfCode = getLinesOfCode(project.getProjectKey());
                        project.setLinesOfCode(linesOfCode);
                        
                        LocalDateTime lastScan = getLastScanDate(project.getProjectKey());
                        project.setLastScanDate(lastScan);
                        
                        projects.add(project);
                        
                        updateProjectSnapshot(project);
                    }
                    
                    int totalPages = jsonResponse.get("paging").get("total").asInt();
                    if (page >= Math.ceil((double) totalPages / pageSize)) {
                        break;
                    }
                    
                    page++;
                }
            }
        } catch (IOException e) {
            log.error("Error fetching projects from SonarQube", e);
            throw new RuntimeException("Failed to fetch projects from SonarQube", e);
        }
        
        return projects;
    }
    
    @Override
    public void deleteProject(String projectKey) {
        String url = String.format("%s/api/projects/delete?project=%s", sonarQubeUrl, projectKey);
        Request request = buildRequest(url);
        
        try (Response response = httpClient.newCall(request).execute()) {
            if (!response.isSuccessful()) {
                throw new RuntimeException("Failed to delete project: " + response.message());
            }
            projectSnapshotRepository.findByProjectKey(projectKey).ifPresent(projectSnapshotRepository::delete);
            log.info("Successfully deleted project: {}", projectKey);
        } catch (IOException e) {
            log.error("Error deleting project from SonarQube", e);
            throw new RuntimeException("Failed to delete project from SonarQube", e);
        }
    }
    
    @Override
    public Long getTotalLinesOfCode() {
        List<ProjectDTO> projects = getAllProjects();
        return projects.stream()
                .mapToLong(p -> p.getLinesOfCode() != null ? p.getLinesOfCode() : 0L)
                .sum();
    }
    
    @Override
    public void syncProjects() {
        getAllProjects();
    }
    
    private Request buildRequest(String url) {
        Request.Builder builder = new Request.Builder().url(url);
        
        if (token != null && !token.isEmpty()) {
            builder.header("Authorization", "Bearer " + token);
        } else {
            String credentials = username + ":" + password;
            String encodedCredentials = Base64.getEncoder()
                    .encodeToString(credentials.getBytes(StandardCharsets.UTF_8));
            builder.header("Authorization", "Basic " + encodedCredentials);
        }
        
        return builder.build();
    }
    
    private Long getLinesOfCode(String projectKey) {
        try {
            String url = String.format("%s/api/measures/component?component=%s&metricKeys=ncloc", 
                    sonarQubeUrl, projectKey);
            Request request = buildRequest(url);
            
            try (Response response = httpClient.newCall(request).execute()) {
                if (response.isSuccessful()) {
                    String responseBody = response.body().string();
                    JsonNode jsonResponse = objectMapper.readTree(responseBody);
                    JsonNode measures = jsonResponse.get("component").get("measures");
                    
                    if (measures != null && measures.isArray()) {
                        for (JsonNode measure : measures) {
                            if ("ncloc".equals(measure.get("metric").asText())) {
                                return Long.parseLong(measure.get("value").asText());
                            }
                        }
                    }
                }
            }
        } catch (Exception e) {
            log.warn("Could not get lines of code for project: {}", projectKey, e);
        }
        
        return 0L;
    }
    
    private LocalDateTime getLastScanDate(String projectKey) {
        try {
            String url = String.format("%s/api/project_analyses/search?project=%s&ps=1", 
                    sonarQubeUrl, projectKey);
            Request request = buildRequest(url);
            
            try (Response response = httpClient.newCall(request).execute()) {
                if (response.isSuccessful()) {
                    String responseBody = response.body().string();
                    JsonNode jsonResponse = objectMapper.readTree(responseBody);
                    JsonNode analyses = jsonResponse.get("analyses");
                    
                    if (analyses != null && analyses.isArray() && analyses.size() > 0) {
                        String dateStr = analyses.get(0).get("date").asText();
                        DateTimeFormatter formatter = DateTimeFormatter.ISO_DATE_TIME;
                        return LocalDateTime.parse(dateStr, formatter);
                    }
                }
            }
        } catch (Exception e) {
            log.warn("Could not get last scan date for project: {}", projectKey, e);
        }
        
        return LocalDateTime.now();
    }
    
    private void updateProjectSnapshot(ProjectDTO project) {
        ProjectSnapshot existing = projectSnapshotRepository.findByProjectKey(project.getProjectKey())
                .orElse(ProjectSnapshot.builder()
                        .projectKey(project.getProjectKey())
                        .build());
        
        existing.setProjectName(project.getProjectName());
        existing.setLinesOfCode(project.getLinesOfCode());
        existing.setLastScanDate(project.getLastScanDate());
        
        projectSnapshotRepository.save(existing);
    }
}

