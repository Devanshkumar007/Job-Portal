package com.capg.AdminService.service;

import com.capg.AdminService.dto.JobResponseDto;
import com.capg.AdminService.dto.PagedResponse;

public interface AdminJobService {
    PagedResponse<JobResponseDto> getAllJobs(String role, int page, int size, String sortBy, String direction);

    JobResponseDto getJobById(String role, Long jobId);

    void deleteJob(String role, Long jobId);
}
