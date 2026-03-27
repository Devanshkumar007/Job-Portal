package com.capg.JobService.service;

import com.capg.JobService.dto.JobFilterDto;
import com.capg.JobService.dto.JobRequestDto;
import com.capg.JobService.dto.JobResponseDto;
import org.springframework.data.domain.Page;

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
}
