package com.capg.ApplicationService.controller;

import com.capg.ApplicationService.dto.DashboardPipelineDto;
import com.capg.ApplicationService.dto.DashboardSummaryDto;
import com.capg.ApplicationService.dto.RecentCandidateDto;
import com.capg.ApplicationService.dto.RecentJobDto;
import com.capg.ApplicationService.dto.RecruiterDashboardDto;
import com.capg.ApplicationService.service.RecruiterDashboardService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/recruiter/dashboard")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Recruiter Dashboard")
public class RecruiterDashboardController {

    private final RecruiterDashboardService recruiterDashboardService;

    @GetMapping("/summary")
    @Operation(summary = "Get recruiter dashboard summary")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Summary fetched"),
            @ApiResponse(responseCode = "403", description = "Only recruiters can access")
    })
    public ResponseEntity<DashboardSummaryDto> getSummary(
            @RequestHeader("X-User-Id") Long recruiterId,
            @RequestHeader("X-User-Role") String role) {
        log.info("Recruiter dashboard summary request: recruiterId={}", recruiterId);
        return ResponseEntity.ok(recruiterDashboardService.getSummary(recruiterId, role));
    }

    @GetMapping("/recent-jobs")
    @Operation(summary = "Get recruiter recent jobs")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Recent jobs fetched"),
            @ApiResponse(responseCode = "403", description = "Only recruiters can access")
    })
    public ResponseEntity<List<RecentJobDto>> getRecentJobs(
            @RequestHeader("X-User-Id") Long recruiterId,
            @RequestHeader("X-User-Role") String role) {
        log.info("Recruiter dashboard recent-jobs request: recruiterId={}", recruiterId);
        return ResponseEntity.ok(recruiterDashboardService.getRecentJobs(recruiterId, role));
    }

    @GetMapping("/recent-candidates")
    @Operation(summary = "Get recruiter recent candidates")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Recent candidates fetched"),
            @ApiResponse(responseCode = "403", description = "Only recruiters can access")
    })
    public ResponseEntity<List<RecentCandidateDto>> getRecentCandidates(
            @RequestHeader("X-User-Id") Long recruiterId,
            @RequestHeader("X-User-Role") String role) {
        log.info("Recruiter dashboard recent-candidates request: recruiterId={}", recruiterId);
        return ResponseEntity.ok(recruiterDashboardService.getRecentCandidates(recruiterId, role));
    }

    @GetMapping("/pipeline")
    @Operation(summary = "Get recruiter application pipeline metrics")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Pipeline metrics fetched"),
            @ApiResponse(responseCode = "403", description = "Only recruiters can access")
    })
    public ResponseEntity<DashboardPipelineDto> getPipeline(
            @RequestHeader("X-User-Id") Long recruiterId,
            @RequestHeader("X-User-Role") String role) {
        log.info("Recruiter dashboard pipeline request: recruiterId={}", recruiterId);
        return ResponseEntity.ok(recruiterDashboardService.getPipeline(recruiterId, role));
    }

    @GetMapping
    @Operation(summary = "Get complete recruiter dashboard payload")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Dashboard payload fetched"),
            @ApiResponse(responseCode = "403", description = "Only recruiters can access")
    })
    public ResponseEntity<RecruiterDashboardDto> getDashboard(
            @RequestHeader("X-User-Id") Long recruiterId,
            @RequestHeader("X-User-Role") String role) {
        log.info("Recruiter dashboard combined request: recruiterId={}", recruiterId);
        return ResponseEntity.ok(recruiterDashboardService.getDashboard(recruiterId, role));
    }
}
