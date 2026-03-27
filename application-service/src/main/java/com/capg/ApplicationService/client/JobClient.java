package com.capg.ApplicationService.client;

import com.capg.ApplicationService.dto.JobDetailsResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@FeignClient(name = "JOB-SERVICE")
public interface JobClient {

    @GetMapping("/api/jobs/{id}")
    JobDetailsResponse getJobById(@PathVariable("id") Long id);
}
