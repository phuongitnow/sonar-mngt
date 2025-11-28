package com.sonarqube.adminapp.presentation.controller;

import com.sonarqube.adminapp.application.dto.StatisticsDTO;
import com.sonarqube.adminapp.application.service.CleanupService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = StatisticsController.class)
@org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc(addFilters = false)
class StatisticsControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private CleanupService cleanupService;

    @Test
    void getStatistics_shouldReturnOk() throws Exception {
        StatisticsDTO dto = StatisticsDTO.builder()
                .totalProjects(5)
                .totalLinesOfCode(700_000L)
                .maxLinesOfCode(1_000_000L)
                .percentageUsage(70.0)
                .projectsToDelete(0)
                .build();
        when(cleanupService.getStatistics()).thenReturn(dto);

        mockMvc.perform(get("/api/statistics").contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalProjects").value(5))
                .andExpect(jsonPath("$.totalLinesOfCode").value(700000))
                .andExpect(jsonPath("$.maxLinesOfCode").value(1000000))
                .andExpect(jsonPath("$.projectsToDelete").value(0));
    }
}


