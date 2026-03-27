package com.capg.AdminService.client;

import com.capg.AdminService.dto.JobResponseDto;
import com.capg.AdminService.dto.PagedResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestParam;

@FeignClient(name="JOB-SERVICE")
public interface JobsClient {

    @DeleteMapping("/api/jobs/{id}")
    void deleteJob(
            @PathVariable Long id ,
            @RequestHeader("X-User-Id") Long recruiterId);

    @GetMapping("/api/jobs")
    PagedResponse<JobResponseDto> getAllJobs(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @RequestParam(defaultValue = "desc") String direction);


    @GetMapping("/api/jobs/{id}")
    JobResponseDto getJobById(
            @PathVariable Long id);



}

