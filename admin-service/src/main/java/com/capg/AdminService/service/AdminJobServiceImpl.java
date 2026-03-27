package com.capg.AdminService.service;

import com.capg.AdminService.client.JobsClient;
import com.capg.AdminService.dto.JobResponseDto;
import com.capg.AdminService.dto.PagedResponse;
import com.capg.AdminService.exception.DownstreamServiceUnavailableException;
import com.capg.AdminService.exception.UnauthorizedException;
import com.capg.AdminService.util.AdminRoleValidator;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class AdminJobServiceImpl implements AdminJobService {

    private final JobsClient jobsClient;

    @Override
    @CircuitBreaker(name = "jobService", fallbackMethod = "getAllJobsFallback")
    public PagedResponse<JobResponseDto> getAllJobs(String role, int page, int size, String sortBy, String direction) {
        AdminRoleValidator.requireAdmin(role);
        return jobsClient.getAllJobs(page, size, sortBy, direction);
    }

    @Override
    @CircuitBreaker(name = "jobService", fallbackMethod = "getJobByIdFallback")
    public JobResponseDto getJobById(String role, Long jobId) {
        AdminRoleValidator.requireAdmin(role);
        return jobsClient.getJobById(jobId);
    }

    @Override
    @CircuitBreaker(name = "jobService", fallbackMethod = "deleteJobFallback")
    public void deleteJob(String role, Long jobId) {
        AdminRoleValidator.requireAdmin(role);
        JobResponseDto job = jobsClient.getJobById(jobId);
        if (job.getRecruiterId() == null) {
            throw new IllegalStateException("Recruiter id is missing for job: " + jobId);
        }
        jobsClient.deleteJob(jobId, job.getRecruiterId());
    }

    private PagedResponse<JobResponseDto> getAllJobsFallback(
            String role, int page, int size, String sortBy, String direction, Throwable ex) {
        if (ex instanceof UnauthorizedException unauthorizedException) {
            throw unauthorizedException;
        }
        log.error("Fallback triggered. service=Job service, operation=getAllJobs, cause={}",
                ex.getMessage(), ex);
        throw new DownstreamServiceUnavailableException(
                "Job service unreachable. Please try again after some time.", ex);
    }

    private JobResponseDto getJobByIdFallback(String role, Long jobId, Throwable ex) {
        if (ex instanceof UnauthorizedException unauthorizedException) {
            throw unauthorizedException;
        }
        log.error("Fallback triggered. service=Job service, operation=getJobById, cause={}",
                ex.getMessage(), ex);
        throw new DownstreamServiceUnavailableException(
                "Job service unreachable. Please try again after some time.", ex);
    }

    private void deleteJobFallback(String role, Long jobId, Throwable ex) {
        if (ex instanceof UnauthorizedException unauthorizedException) {
            throw unauthorizedException;
        }
        if (ex instanceof IllegalStateException illegalStateException) {
            throw illegalStateException;
        }
        log.error("Fallback triggered. service=Job service, operation=deleteJob, cause={}",
                ex.getMessage(), ex);
        throw new DownstreamServiceUnavailableException(
                "Job service unreachable. Please try again after some time.", ex);
    }
}
