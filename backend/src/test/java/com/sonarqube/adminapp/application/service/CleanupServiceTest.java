package com.sonarqube.adminapp.application.service;

import com.sonarqube.adminapp.application.dto.CleanupResultDTO;
import com.sonarqube.adminapp.application.dto.StatisticsDTO;
import com.sonarqube.adminapp.domain.repository.CleanupHistoryRepository;
import com.sonarqube.adminapp.domain.repository.ProjectSnapshotRepository;
import com.sonarqube.adminapp.infrastructure.email.EmailService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Collections;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@org.mockito.junit.jupiter.MockitoSettings(strictness = org.mockito.quality.Strictness.LENIENT)
class CleanupServiceTest {

    @Mock
    private ProjectSnapshotRepository projectSnapshotRepository;
    @Mock
    private CleanupHistoryRepository cleanupHistoryRepository;
    @Mock
    private SonarQubeService sonarQubeService;
    @Mock
    private EmailService emailService;

    @InjectMocks
    private CleanupService cleanupService;

    @BeforeEach
    void setup() {
        // default stubs
        when(projectSnapshotRepository.count()).thenReturn(5L);
        when(projectSnapshotRepository.getTotalLinesOfCode()).thenReturn(900_000L); // below limit
        when(projectSnapshotRepository.findProjectsOlderThan(any(LocalDateTime.class)))
                .thenReturn(Collections.emptyList());
    }

    @Test
    void performCleanup_shouldSkip_whenTotalLOCBelowLimit() {
        CleanupResultDTO result = cleanupService.performCleanup();

        assertThat(result.getStatus()).isEqualTo("SKIPPED");
        assertThat(result.getProjectsDeleted()).isEqualTo(0);
        assertThat(result.getLinesOfCodeRemaining()).isEqualTo(900_000L);

        verify(sonarQubeService, times(1)).syncProjects();
        verify(cleanupHistoryRepository, atLeastOnce()).save(any());
        verifyNoInteractions(emailService);
    }

    @Test
    void getStatistics_shouldReturnDerivedMetrics() {
        when(projectSnapshotRepository.count()).thenReturn(10L);
        when(projectSnapshotRepository.getTotalLinesOfCode()).thenReturn(500_000L);
        when(projectSnapshotRepository.findProjectsOlderThan(any(LocalDateTime.class)))
                .thenReturn(Collections.emptyList());

        StatisticsDTO stats = cleanupService.getStatistics();

        assertThat(stats.getTotalProjects()).isEqualTo(10);
        assertThat(stats.getTotalLinesOfCode()).isEqualTo(500_000L);
        assertThat(stats.getMaxLinesOfCode()).isEqualTo(1_000_000L);
        assertThat(stats.getPercentageUsage()).isGreaterThan(0.0);
    }
}


