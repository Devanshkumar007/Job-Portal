package com.capg.JobService.service;

import com.capg.JobService.dto.JobFilterDto;
import com.capg.JobService.dto.JobRequestDto;
import com.capg.JobService.dto.JobResponseDto;
import com.capg.JobService.dto.RecruiterJobSummaryDto;
import com.capg.JobService.dto.RecruiterOpenRolesCountDto;
import com.capg.JobService.dto.RecruiterRecentJobDto;
import org.springframework.data.domain.Page;

import java.util.List;

public interface JobService {
    JobResponseDto createJob(JobRequestDto dto, Long recruiterId, String role);

    Page<JobResponseDto> getAllJobs(int page, int size, String sortBy, String direction);

    JobResponseDto getJobById(Long id);

    JobResponseDto updateJob(Long id, JobRequestDto dto, Long recruiterId);

    void deleteJob(Long id, Long recruiterId);

    Page<JobResponseDto> searchJobs(
            JobFilterDto filter,
            int page,
            int size,
            String sortBy,
            String direction);

    List<Long> getRecruiterJobIds(Long recruiterId, Long requesterId, String role);

    RecruiterOpenRolesCountDto getRecruiterOpenRolesCount(Long recruiterId, Long requesterId, String role);

    List<RecruiterRecentJobDto> getRecruiterRecentJobs(Long recruiterId, int limit, Long requesterId, String role);

    RecruiterJobSummaryDto getRecruiterJobSummary(Long recruiterId, Long requesterId, String role);

    List<JobResponseDto> getJobsByRecruiterId(Long recruiterId);
}
