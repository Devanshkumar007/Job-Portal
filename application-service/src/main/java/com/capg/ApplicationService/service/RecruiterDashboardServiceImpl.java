package com.capg.ApplicationService.service;

import com.capg.ApplicationService.client.JobClient;
import com.capg.ApplicationService.dto.DashboardPipelineDto;
import com.capg.ApplicationService.dto.DashboardSummaryDto;
import com.capg.ApplicationService.dto.RecentCandidateDto;
import com.capg.ApplicationService.dto.RecentJobDto;
import com.capg.ApplicationService.dto.RecruiterDashboardDto;
import com.capg.ApplicationService.dto.RecruiterOpenRolesCountDto;
import com.capg.ApplicationService.entity.Application;
import com.capg.ApplicationService.entity.ApplicationStatus;
import com.capg.ApplicationService.exception.UnauthorizedException;
import com.capg.ApplicationService.repository.ApplicationRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class RecruiterDashboardServiceImpl implements RecruiterDashboardService {

    private static final String ROLE_RECRUITER = "RECRUITER";
    private static final int RECENT_JOBS_LIMIT = 3;

    private final ApplicationRepository applicationRepository;
    private final JobClient jobClient;

    @Override
    public DashboardSummaryDto getSummary(Long recruiterId, String role) {
        assertRecruiter(role);
        List<Long> jobIds = fetchRecruiterJobIds(recruiterId, role);
        long openRoles = fetchOpenRolesCount(recruiterId, role);
        return buildSummary(openRoles, jobIds);
    }

    @Override
    public List<RecentJobDto> getRecentJobs(Long recruiterId, String role) {
        assertRecruiter(role);
        return fetchRecentJobs(recruiterId, role);
    }

    @Override
    public List<RecentCandidateDto> getRecentCandidates(Long recruiterId, String role) {
        assertRecruiter(role);
        List<Long> jobIds = fetchRecruiterJobIds(recruiterId, role);
        return buildRecentCandidates(jobIds);
    }

    @Override
    public DashboardPipelineDto getPipeline(Long recruiterId, String role) {
        assertRecruiter(role);
        List<Long> jobIds = fetchRecruiterJobIds(recruiterId, role);
        return buildPipeline(jobIds);
    }

    @Override
    public RecruiterDashboardDto getDashboard(Long recruiterId, String role) {
        assertRecruiter(role);
        List<Long> jobIds = fetchRecruiterJobIds(recruiterId, role);
        long openRoles = fetchOpenRolesCount(recruiterId, role);
        List<RecentJobDto> recentJobs = fetchRecentJobs(recruiterId, role);

        return new RecruiterDashboardDto(
                buildSummary(openRoles, jobIds),
                recentJobs,
                buildRecentCandidates(jobIds),
                buildPipeline(jobIds)
        );
    }

    private DashboardSummaryDto buildSummary(long openRoles, List<Long> jobIds) {
        if (jobIds.isEmpty()) {
            return new DashboardSummaryDto(openRoles, 0, 0, 0);
        }

        long totalApplications = applicationRepository.countByJobIdIn(jobIds);
        long shortlistedCount = applicationRepository.countByJobIdInAndStatus(jobIds, ApplicationStatus.SHORTLISTED);
        long offersSent = applicationRepository.countByJobIdInAndStatus(jobIds, ApplicationStatus.OFFERED);

        return new DashboardSummaryDto(
                openRoles,
                totalApplications,
                shortlistedCount,
                offersSent
        );
    }

    private List<RecentCandidateDto> buildRecentCandidates(List<Long> jobIds) {
        if (jobIds.isEmpty()) {
            return Collections.emptyList();
        }

        return applicationRepository.findTop3ByJobIdInOrderByAppliedAtDesc(jobIds)
                .stream()
                .map(this::toRecentCandidateDto)
                .toList();
    }

    private DashboardPipelineDto buildPipeline(List<Long> jobIds) {
        if (jobIds.isEmpty()) {
            return new DashboardPipelineDto(0, 0, 0);
        }

        long totalApplications = applicationRepository.countByJobIdIn(jobIds);
        if (totalApplications == 0) {
            return new DashboardPipelineDto(0, 0, 0);
        }

        long interviewsScheduled = applicationRepository.countByJobIdInAndStatus(
                jobIds, ApplicationStatus.INTERVIEW_SCHEDULED);
        long offersSent = applicationRepository.countByJobIdInAndStatus(jobIds, ApplicationStatus.OFFERED);

        return new DashboardPipelineDto(
                toPercent(totalApplications, totalApplications),
                toPercent(interviewsScheduled, totalApplications),
                toPercent(offersSent, totalApplications)
        );
    }

    private RecentCandidateDto toRecentCandidateDto(Application app) {
        return new RecentCandidateDto(
                app.getId(),
                app.getJobId(),
                nonNull(app.getApplicantName()),
                nonNull(app.getApplicantEmail()),
                nonNull(app.getJobTitle()),
                app.getStatus() == null ? "" : app.getStatus().name()
        );
    }

    private List<Long> fetchRecruiterJobIds(Long recruiterId, String role) {
        List<Long> jobIds = jobClient.getRecruiterJobIds(recruiterId, recruiterId, role);
        return jobIds == null ? Collections.emptyList() : jobIds;
    }

    private long fetchOpenRolesCount(Long recruiterId, String role) {
        RecruiterOpenRolesCountDto response = jobClient.getRecruiterOpenRolesCount(recruiterId, recruiterId, role);
        return response == null ? 0 : response.getOpenRoles();
    }

    private List<RecentJobDto> fetchRecentJobs(Long recruiterId, String role) {
        List<RecentJobDto> jobs = jobClient.getRecruiterRecentJobs(recruiterId, recruiterId, role, RECENT_JOBS_LIMIT);
        if (jobs == null) {
            return Collections.emptyList();
        }

        return jobs.stream()
                .map(job -> new RecentJobDto(
                        job.getJobId(),
                        nonNull(job.getTitle()),
                        nonNull(job.getCompanyName()),
                        nonNull(job.getStatus())
                ))
                .toList();
    }

    private int toPercent(long value, long total) {
        if (total <= 0) {
            return 0;
        }
        return (int) Math.round((value * 100.0) / total);
    }

    private void assertRecruiter(String role) {
        if (!ROLE_RECRUITER.equalsIgnoreCase(role)) {
            throw new UnauthorizedException("Only recruiters can access this resource");
        }
    }

    private String nonNull(String value) {
        return value == null ? "" : value;
    }
}
