package com.capg.ApplicationService.controller;

import com.capg.ApplicationService.dto.DashboardPipelineDto;
import com.capg.ApplicationService.dto.DashboardSummaryDto;
import com.capg.ApplicationService.dto.RecentCandidateDto;
import com.capg.ApplicationService.dto.RecentJobDto;
import com.capg.ApplicationService.dto.RecruiterDashboardDto;
import com.capg.ApplicationService.exception.GlobalExceptionHandler;
import com.capg.ApplicationService.service.RecruiterDashboardService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = RecruiterDashboardController.class)
@Import(GlobalExceptionHandler.class)
class RecruiterDashboardControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private RecruiterDashboardService recruiterDashboardService;

    @Test
    void get_summary_returns_ok() throws Exception {
        when(recruiterDashboardService.getSummary(91L, "RECRUITER"))
                .thenReturn(new DashboardSummaryDto(4, 20, 5, 2));

        mockMvc.perform(get("/api/recruiter/dashboard/summary")
                        .header("X-User-Id", "91")
                        .header("X-User-Role", "RECRUITER"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.openRoles").value(4));
    }

    @Test
    void get_recent_jobs_returns_ok() throws Exception {
        when(recruiterDashboardService.getRecentJobs(91L, "RECRUITER"))
                .thenReturn(List.of(new RecentJobDto(201L, "SDE", "Acme", "OPEN")));

        mockMvc.perform(get("/api/recruiter/dashboard/recent-jobs")
                        .header("X-User-Id", "91")
                        .header("X-User-Role", "RECRUITER"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].jobId").value(201));
    }

    @Test
    void get_recent_candidates_returns_ok() throws Exception {
        when(recruiterDashboardService.getRecentCandidates(91L, "RECRUITER"))
                .thenReturn(List.of(new RecentCandidateDto(1L, 201L, "Alex", "alex@test.com", "SDE", "APPLIED")));

        mockMvc.perform(get("/api/recruiter/dashboard/recent-candidates")
                        .header("X-User-Id", "91")
                        .header("X-User-Role", "RECRUITER"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].applicationId").value(1));
    }

    @Test
    void get_pipeline_returns_ok() throws Exception {
        when(recruiterDashboardService.getPipeline(91L, "RECRUITER"))
                .thenReturn(new DashboardPipelineDto(70, 20, 10));

        mockMvc.perform(get("/api/recruiter/dashboard/pipeline")
                        .header("X-User-Id", "91")
                        .header("X-User-Role", "RECRUITER"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.applicationsReceivedPercent").value(70));
    }

    @Test
    void get_dashboard_returns_ok() throws Exception {
        RecruiterDashboardDto dto = new RecruiterDashboardDto(
                new DashboardSummaryDto(4, 20, 5, 2),
                List.of(new RecentJobDto(201L, "SDE", "Acme", "OPEN")),
                List.of(new RecentCandidateDto(1L, 201L, "Alex", "alex@test.com", "SDE", "APPLIED")),
                new DashboardPipelineDto(70, 20, 10)
        );
        when(recruiterDashboardService.getDashboard(91L, "RECRUITER")).thenReturn(dto);

        mockMvc.perform(get("/api/recruiter/dashboard")
                        .header("X-User-Id", "91")
                        .header("X-User-Role", "RECRUITER"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.summary.openRoles").value(4))
                .andExpect(jsonPath("$.recentJobs[0].jobId").value(201))
                .andExpect(jsonPath("$.recentCandidates[0].applicationId").value(1))
                .andExpect(jsonPath("$.pipeline.offersSentPercent").value(10));
    }
}

