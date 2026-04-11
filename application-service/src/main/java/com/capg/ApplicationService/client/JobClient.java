package com.capg.ApplicationService.client;

import com.capg.ApplicationService.dto.JobDetailsResponse;
import com.capg.ApplicationService.dto.RecentJobDto;
import com.capg.ApplicationService.dto.RecruiterOpenRolesCountDto;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

@FeignClient(name = "JOB-SERVICE")
public interface JobClient {

    @GetMapping("/api/jobs/{id}")
    JobDetailsResponse getJobById(@PathVariable("id") Long id);

    @GetMapping("/api/internal/recruiters/{recruiterId}/jobs/ids")
    List<Long> getRecruiterJobIds(@PathVariable("recruiterId") Long recruiterId,
                                  @RequestHeader("X-User-Id") Long requesterId,
                                  @RequestHeader("X-User-Role") String role);

    @GetMapping("/api/internal/recruiters/{recruiterId}/jobs/count")
    RecruiterOpenRolesCountDto getRecruiterOpenRolesCount(@PathVariable("recruiterId") Long recruiterId,
                                                          @RequestHeader("X-User-Id") Long requesterId,
                                                          @RequestHeader("X-User-Role") String role);

    @GetMapping("/api/internal/recruiters/{recruiterId}/jobs/recent")
    List<RecentJobDto> getRecruiterRecentJobs(@PathVariable("recruiterId") Long recruiterId,
                                              @RequestHeader("X-User-Id") Long requesterId,
                                              @RequestHeader("X-User-Role") String role,
                                              @RequestParam("limit") int limit);
}
