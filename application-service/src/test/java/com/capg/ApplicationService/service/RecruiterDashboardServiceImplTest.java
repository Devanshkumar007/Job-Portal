package com.capg.ApplicationService.service;

import com.capg.ApplicationService.client.JobClient;
import com.capg.ApplicationService.dto.DashboardPipelineDto;
import com.capg.ApplicationService.dto.DashboardSummaryDto;
import com.capg.ApplicationService.dto.RecentCandidateDto;
import com.capg.ApplicationService.dto.RecentJobDto;
import com.capg.ApplicationService.dto.RecruiterOpenRolesCountDto;
import com.capg.ApplicationService.entity.Application;
import com.capg.ApplicationService.entity.ApplicationStatus;
import com.capg.ApplicationService.exception.UnauthorizedException;
import com.capg.ApplicationService.repository.ApplicationRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class RecruiterDashboardServiceImplTest {

    @Mock
    private ApplicationRepository applicationRepository;

    @Mock
    private JobClient jobClient;

    @InjectMocks
    private RecruiterDashboardServiceImpl service;

    @Test
    void getSummary_when_no_jobs_returns_zero_values() {
        when(jobClient.getRecruiterJobIds(77L, 77L, "RECRUITER")).thenReturn(List.of());
        when(jobClient.getRecruiterOpenRolesCount(77L, 77L, "RECRUITER"))
                .thenReturn(openRoles(4));

        DashboardSummaryDto summary = service.getSummary(77L, "RECRUITER");

        assertEquals(4, summary.getOpenRoles());
        assertEquals(0, summary.getTotalApplications());
        assertEquals(0, summary.getShortlistedCount());
        assertEquals(0, summary.getOffersSent());
        verify(applicationRepository, never()).countByJobIdIn(List.of());
    }

    @Test
    void getRecentCandidates_returns_top_three_for_recruiter_jobs() {
        when(jobClient.getRecruiterJobIds(77L, 77L, "RECRUITER")).thenReturn(List.of(11L, 12L));

        Application a1 = app(1L, "Pawan Singh", "p1@example.com", "Java", ApplicationStatus.APPLIED);
        Application a2 = app(2L, "Neha", "p2@example.com", "Java", ApplicationStatus.SHORTLISTED);
        Application a3 = app(3L, "Ravi", "p3@example.com", "Java", ApplicationStatus.INTERVIEW_SCHEDULED);
        when(applicationRepository.findTop3ByJobIdInOrderByAppliedAtDesc(List.of(11L, 12L)))
                .thenReturn(List.of(a1, a2, a3));

        List<RecentCandidateDto> candidates = service.getRecentCandidates(77L, "RECRUITER");

        assertEquals(3, candidates.size());
        assertEquals(11L, candidates.get(0).getJobId());
        assertEquals("Pawan Singh", candidates.get(0).getApplicantName());
        assertEquals("APPLIED", candidates.get(0).getStatus());
    }

    @Test
    void getPipeline_calculates_percentages_from_application_counts() {
        when(jobClient.getRecruiterJobIds(77L, 77L, "RECRUITER")).thenReturn(List.of(11L));
        when(applicationRepository.countByJobIdIn(List.of(11L))).thenReturn(82L);
        when(applicationRepository.countByJobIdInAndStatus(List.of(11L), ApplicationStatus.INTERVIEW_SCHEDULED))
                .thenReturn(54L);
        when(applicationRepository.countByJobIdInAndStatus(List.of(11L), ApplicationStatus.OFFERED))
                .thenReturn(18L);

        DashboardPipelineDto pipeline = service.getPipeline(77L, "RECRUITER");

        assertEquals(100, pipeline.getApplicationsReceivedPercent());
        assertEquals(66, pipeline.getInterviewsScheduledPercent());
        assertEquals(22, pipeline.getOffersSentPercent());
    }

    @Test
    void getDashboard_non_recruiter_throws_unauthorized() {
        assertThrows(UnauthorizedException.class, () -> service.getDashboard(77L, "ADMIN"));
    }

    @Test
    void getRecentJobs_uses_internal_recent_endpoint_with_role_header() {
        when(jobClient.getRecruiterRecentJobs(77L, 77L, "RECRUITER", 3))
                .thenReturn(List.of(
                        new RecentJobDto(11L, "Java Dev", "Acme", "ACTIVE"),
                        new RecentJobDto(12L, "Node Dev", "Globex", "DRAFT")
                ));

        List<RecentJobDto> result = service.getRecentJobs(77L, "RECRUITER");

        assertEquals(2, result.size());
        assertEquals(11L, result.get(0).getJobId());
        assertEquals("ACTIVE", result.get(0).getStatus());
        verify(jobClient).getRecruiterRecentJobs(77L, 77L, "RECRUITER", 3);
    }

    @Test
    void getDashboard_combines_internal_job_endpoints_and_application_counts() {
        when(jobClient.getRecruiterJobIds(77L, 77L, "RECRUITER")).thenReturn(List.of(11L, 12L));
        when(jobClient.getRecruiterOpenRolesCount(77L, 77L, "RECRUITER")).thenReturn(openRoles(9));
        when(jobClient.getRecruiterRecentJobs(77L, 77L, "RECRUITER", 3))
                .thenReturn(List.of(new RecentJobDto(12L, "Node Dev", "Globex", "ACTIVE")));
        when(applicationRepository.countByJobIdIn(List.of(11L, 12L))).thenReturn(5L);
        when(applicationRepository.countByJobIdInAndStatus(List.of(11L, 12L), ApplicationStatus.SHORTLISTED))
                .thenReturn(2L);
        when(applicationRepository.countByJobIdInAndStatus(List.of(11L, 12L), ApplicationStatus.OFFERED))
                .thenReturn(1L);
        when(applicationRepository.countByJobIdInAndStatus(List.of(11L, 12L), ApplicationStatus.INTERVIEW_SCHEDULED))
                .thenReturn(3L);
        when(applicationRepository.findTop3ByJobIdInOrderByAppliedAtDesc(List.of(11L, 12L)))
                .thenReturn(List.of());

        var dashboard = service.getDashboard(77L, "RECRUITER");

        assertEquals(9, dashboard.getSummary().getOpenRoles());
        assertEquals(5, dashboard.getSummary().getTotalApplications());
        assertEquals(1, dashboard.getRecentJobs().size());
        assertEquals(60, dashboard.getPipeline().getInterviewsScheduledPercent());
        verify(jobClient).getRecruiterJobIds(77L, 77L, "RECRUITER");
        verify(jobClient).getRecruiterOpenRolesCount(77L, 77L, "RECRUITER");
        verify(jobClient).getRecruiterRecentJobs(77L, 77L, "RECRUITER", 3);
    }

    private RecruiterOpenRolesCountDto openRoles(long openRoles) {
        RecruiterOpenRolesCountDto dto = new RecruiterOpenRolesCountDto();
        dto.setOpenRoles(openRoles);
        return dto;
    }

    private Application app(Long id,
                            String name,
                            String email,
                            String jobTitle,
                            ApplicationStatus status) {
        Application application = new Application();
        application.setId(id);
        application.setApplicantName(name);
        application.setApplicantEmail(email);
        application.setJobId(11L);
        application.setJobTitle(jobTitle);
        application.setStatus(status);
        return application;
    }
}
