package com.capg.JobService.controller;

import com.capg.JobService.dto.RecruiterOpenRolesCountDto;
import com.capg.JobService.dto.RecruiterRecentJobDto;
import com.capg.JobService.dto.RecruiterJobSummaryDto;
import com.capg.JobService.service.JobService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/internal/recruiters")
@RequiredArgsConstructor
@Slf4j
public class InternalRecruiterController {

    private final JobService jobService;

    @GetMapping("/{recruiterId}/jobs/ids")
    public ResponseEntity<List<Long>> getRecruiterJobIds(
            @PathVariable Long recruiterId,
            @RequestHeader("X-User-Role") String role,
            @RequestHeader(value = "X-User-Id", required = false) Long requesterId
    ) {
        log.info("Internal recruiter job ids requested for recruiterId={}", recruiterId);
        return ResponseEntity.ok(jobService.getRecruiterJobIds(recruiterId, requesterId, role));
    }

    @GetMapping("/{recruiterId}/jobs/count")
    public ResponseEntity<RecruiterOpenRolesCountDto> getRecruiterOpenRolesCount(
            @PathVariable Long recruiterId,
            @RequestHeader("X-User-Role") String role,
            @RequestHeader(value = "X-User-Id", required = false) Long requesterId
    ) {
        log.info("Internal recruiter open roles count requested for recruiterId={}", recruiterId);
        return ResponseEntity.ok(jobService.getRecruiterOpenRolesCount(recruiterId, requesterId, role));
    }

    @GetMapping("/{recruiterId}/jobs/recent")
    public ResponseEntity<List<RecruiterRecentJobDto>> getRecruiterRecentJobs(
            @PathVariable Long recruiterId,
            @RequestHeader("X-User-Role") String role,
            @RequestHeader(value = "X-User-Id", required = false) Long requesterId,
            @RequestParam(defaultValue = "5") int limit
    ) {
        log.info("Internal recruiter recent jobs requested for recruiterId={}, limit={}", recruiterId, limit);
        return ResponseEntity.ok(jobService.getRecruiterRecentJobs(recruiterId, limit, requesterId, role));
    }

    @GetMapping("/{recruiterId}/jobs/summary")
    public ResponseEntity<RecruiterJobSummaryDto> getRecruiterJobSummary(
            @PathVariable Long recruiterId,
            @RequestHeader("X-User-Role") String role,
            @RequestHeader(value = "X-User-Id", required = false) Long requesterId
    ) {
        log.info("Internal recruiter job summary requested for recruiterId={}", recruiterId);
        return ResponseEntity.ok(jobService.getRecruiterJobSummary(recruiterId, requesterId, role));
    }
}
