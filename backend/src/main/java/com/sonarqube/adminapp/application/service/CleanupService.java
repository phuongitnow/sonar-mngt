package com.sonarqube.adminapp.application.service;

import com.sonarqube.adminapp.application.dto.CleanupResultDTO;
import com.sonarqube.adminapp.application.dto.StatisticsDTO;
import com.sonarqube.adminapp.domain.entity.CleanupHistory;
import com.sonarqube.adminapp.domain.repository.CleanupHistoryRepository;
import com.sonarqube.adminapp.domain.repository.ProjectSnapshotRepository;
import com.sonarqube.adminapp.infrastructure.email.EmailService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class CleanupService {
    
    private final ProjectSnapshotRepository projectSnapshotRepository;
    private final CleanupHistoryRepository cleanupHistoryRepository;
    private final SonarQubeService sonarQubeService;
    private final EmailService emailService;
    
    @Value("${app.cleanup.days-threshold}")
    private int daysThreshold;
    
    @Value("${app.cleanup.notification-email}")
    private String notificationEmail;
    
    private static final long MAX_LINES_OF_CODE = 1_000_000L;
    
    @Transactional
    public CleanupResultDTO performCleanup() {
        log.info("Starting cleanup process...");
        
        CleanupHistory history = CleanupHistory.builder()
                .status("RUNNING")
                .build();
        
        try {
            // Sync projects from SonarQube
            sonarQubeService.syncProjects();
            
            // Get statistics
            StatisticsDTO statistics = getStatistics();
            
            if (statistics.getTotalLinesOfCode() < MAX_LINES_OF_CODE) {
                history.setStatus("SKIPPED");
                history.setErrorMessage("Total LOC is below limit: " + statistics.getTotalLinesOfCode());
                
                CleanupResultDTO result = CleanupResultDTO.builder()
                        .projectsDeleted(0)
                        .linesOfCodeDeleted(0L)
                        .linesOfCodeRemaining(statistics.getTotalLinesOfCode())
                        .status("SKIPPED")
                        .message("No cleanup needed. Total LOC: " + statistics.getTotalLinesOfCode())
                        .build();
                
                cleanupHistoryRepository.save(history);
                return result;
            }
            
            // Calculate how many LOC we need to delete
            long linesOfCodeToDelete = statistics.getTotalLinesOfCode() - MAX_LINES_OF_CODE + 100_000; // Add buffer
            
            // Get projects older than threshold
            LocalDateTime thresholdDate = LocalDateTime.now().minusDays(daysThreshold);
            List<com.sonarqube.adminapp.domain.entity.ProjectSnapshot> projectsToDelete = 
                    projectSnapshotRepository.findProjectsOlderThan(thresholdDate);
            
            int deletedCount = 0;
            long deletedLOC = 0L;
            
            for (com.sonarqube.adminapp.domain.entity.ProjectSnapshot project : projectsToDelete) {
                if (deletedLOC >= linesOfCodeToDelete) {
                    break;
                }
                
                try {
                    sonarQubeService.deleteProject(project.getProjectKey());
                    deletedCount++;
                    deletedLOC += project.getLinesOfCode() != null ? project.getLinesOfCode() : 0L;
                } catch (Exception e) {
                    log.error("Failed to delete project: {}", project.getProjectKey(), e);
                }
            }
            
            // Update statistics
            sonarQubeService.syncProjects();
            long remainingLOC = projectSnapshotRepository.getTotalLinesOfCode();
            
            history.setStatus("COMPLETED");
            history.setProjectsDeleted(deletedCount);
            history.setLinesOfCodeDeleted(deletedLOC);
            history.setLinesOfCodeRemaining(remainingLOC);
            
            // Send notification email
            emailService.sendCleanupNotification(deletedCount, deletedLOC, remainingLOC, notificationEmail);
            
            CleanupResultDTO result = CleanupResultDTO.builder()
                    .projectsDeleted(deletedCount)
                    .linesOfCodeDeleted(deletedLOC)
                    .linesOfCodeRemaining(remainingLOC)
                    .status("COMPLETED")
                    .message("Successfully deleted " + deletedCount + " projects. Remaining LOC: " + remainingLOC)
                    .build();
            
            cleanupHistoryRepository.save(history);
            log.info("Cleanup completed: {}", result);
            
            return result;
            
        } catch (Exception e) {
            log.error("Cleanup failed", e);
            history.setStatus("FAILED");
            history.setErrorMessage(e.getMessage());
            cleanupHistoryRepository.save(history);
            
            throw new RuntimeException("Cleanup failed", e);
        }
    }
    
    public StatisticsDTO getStatistics() {
        Integer totalProjects = (int) projectSnapshotRepository.count();
        Long totalLinesOfCode = projectSnapshotRepository.getTotalLinesOfCode();
        totalLinesOfCode = totalLinesOfCode != null ? totalLinesOfCode : 0L;
        
        double percentageUsage = (totalLinesOfCode * 100.0) / MAX_LINES_OF_CODE;
        
        LocalDateTime thresholdDate = LocalDateTime.now().minusDays(daysThreshold);
        List<com.sonarqube.adminapp.domain.entity.ProjectSnapshot> projectsToDelete = 
                projectSnapshotRepository.findProjectsOlderThan(thresholdDate);
        
        return StatisticsDTO.builder()
                .totalProjects(totalProjects)
                .totalLinesOfCode(totalLinesOfCode)
                .maxLinesOfCode(MAX_LINES_OF_CODE)
                .percentageUsage(percentageUsage)
                .projectsToDelete(projectsToDelete.size())
                .build();
    }
}

