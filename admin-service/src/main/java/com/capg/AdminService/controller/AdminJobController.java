package com.capg.AdminService.controller;

import com.capg.AdminService.dto.JobResponseDto;
import com.capg.AdminService.dto.PagedResponse;
import com.capg.AdminService.service.AdminJobService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/admin/jobs")
@RequiredArgsConstructor
@Tag(name = "Admin Jobs", description = "Admin operations for jobs")
@Slf4j
public class AdminJobController {

    private final AdminJobService adminJobService;

    @GetMapping
    @Operation(summary = "Get all jobs", description = "Fetch paginated jobs for admin users")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Jobs fetched successfully"),
            @ApiResponse(responseCode = "403", description = "Only admins can access this resource"),
            @ApiResponse(responseCode = "503", description = "Job service unreachable")
    })
    public ResponseEntity<PagedResponse<JobResponseDto>> getAllJobs(
            @RequestHeader("X-User-Id") Long requesterId,
            @RequestHeader("X-User-Role") String role,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @RequestParam(defaultValue = "desc") String direction) {
        log.info(
                "Admin jobs request: action=getAllJobs, requesterId={}, role={}, page={}, size={}, sortBy={}, direction={}",
                requesterId, role, page, size, sortBy, direction
        );
        return ResponseEntity.ok(adminJobService.getAllJobs(role, page, size, sortBy, direction));
    }

    @GetMapping("/{jobId}")
    @Operation(summary = "Get job by id", description = "Fetch job details by id")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Job fetched successfully"),
            @ApiResponse(responseCode = "403", description = "Only admins can access this resource"),
            @ApiResponse(responseCode = "503", description = "Job service unreachable")
    })
    public ResponseEntity<JobResponseDto> getJobById(
            @RequestHeader("X-User-Id") Long requesterId,
            @RequestHeader("X-User-Role") String role,
            @PathVariable Long jobId) {
        log.info("Admin jobs request: action=getJobById, jobId={}, requesterId={}, role={}",
                jobId, requesterId, role);
        return ResponseEntity.ok(adminJobService.getJobById(role, jobId));
    }

    @DeleteMapping("/{jobId}")
    @Operation(summary = "Delete job by id", description = "Delete a job by id")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Job deleted successfully"),
            @ApiResponse(responseCode = "403", description = "Only admins can access this resource"),
            @ApiResponse(responseCode = "503", description = "Job service unreachable")
    })
    public ResponseEntity<Void> deleteJob(
            @RequestHeader("X-User-Id") Long requesterId,
            @RequestHeader("X-User-Role") String role,
            @PathVariable Long jobId) {
        log.info("Admin jobs request: action=deleteJob, jobId={}, requesterId={}, role={}",
                jobId, requesterId, role);
        adminJobService.deleteJob(role, jobId);
        return ResponseEntity.noContent().build();
    }
}
