package com.sonarqube.adminapp.presentation.controller;

import com.sonarqube.adminapp.application.dto.CleanupResultDTO;
import com.sonarqube.adminapp.application.service.CleanupService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = CleanupController.class)
@org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc(addFilters = false)
class CleanupControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private CleanupService cleanupService;

    @Test
    void executeCleanup_shouldReturnOk() throws Exception {
        CleanupResultDTO dto = CleanupResultDTO.builder()
                .status("SKIPPED")
                .projectsDeleted(0)
                .linesOfCodeRemaining(800_000L)
                .build();
        when(cleanupService.performCleanup()).thenReturn(dto);

        mockMvc.perform(post("/api/cleanup/execute").contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("SKIPPED"))
                .andExpect(jsonPath("$.projectsDeleted").value(0))
                .andExpect(jsonPath("$.linesOfCodeRemaining").value(800000));
    }
}


