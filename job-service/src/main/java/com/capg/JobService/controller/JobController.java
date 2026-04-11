package com.capg.JobService.controller;

import com.capg.JobService.dto.JobFilterDto;
import com.capg.JobService.dto.JobRequestDto;
import com.capg.JobService.dto.JobResponseDto;
import com.capg.JobService.service.JobService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/jobs")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Jobs", description = "APIs for job posting, retrieval, update, deletion and search")
public class JobController {

    private final JobService jobService;


    @PostMapping
    @Operation(summary = "Create a job", description = "Creates a new job post. Only recruiter role is allowed.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Job created successfully",
                    content = @Content(schema = @Schema(implementation = JobResponseDto.class))),
            @ApiResponse(responseCode = "400", description = "Invalid request data"),
            @ApiResponse(responseCode = "403", description = "Only recruiters can create jobs")
    })
    public ResponseEntity<JobResponseDto> createJob(
            @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    description = "Job payload to create",
                    required = true)
            @Valid @RequestBody JobRequestDto dto,
            @RequestHeader("X-User-Id") Long recruiterId,
            @RequestHeader("X-User-Role") String role) {

        log.info("Create job request received for recruiterId={}, role={}", recruiterId, role);
        JobResponseDto created = jobService.createJob(dto, recruiterId, role);
        log.info("Job created successfully with id={}", created.getId());
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    @GetMapping
    @Operation(summary = "Get all jobs", description = "Returns paginated and sorted job listings.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Jobs fetched successfully")
    })
    public ResponseEntity<Page<JobResponseDto>> getAllJobs(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @RequestParam(defaultValue = "desc") String direction) {

        log.info("Get all jobs request received: page={}, size={}, sortBy={}, direction={}", page, size, sortBy, direction);
        Page<JobResponseDto> jobs = jobService.getAllJobs(page, size, sortBy, direction);
        log.info("Get all jobs request completed: returnedElements={}", jobs.getNumberOfElements());
        return ResponseEntity.ok(jobs);
    }

    @GetMapping(params = "recruiterId")
    @Operation(summary = "Get jobs by recruiter ID", description = "Returns all jobs posted by the given recruiter.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Recruiter jobs fetched successfully")
    })
    public ResponseEntity<List<JobResponseDto>> getJobsByRecruiterId(
            @RequestParam Long recruiterId) {
        log.info("Get jobs by recruiter request received: recruiterId={}", recruiterId);
        List<JobResponseDto> jobs = jobService.getJobsByRecruiterId(recruiterId);
        log.info("Get jobs by recruiter request completed: recruiterId={}, count={}", recruiterId, jobs.size());
        return ResponseEntity.ok(jobs);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get job by ID", description = "Fetches a job by its unique identifier.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Job fetched successfully",
                    content = @Content(schema = @Schema(implementation = JobResponseDto.class))),
            @ApiResponse(responseCode = "404", description = "Job not found")
    })
    public ResponseEntity<JobResponseDto> getJobById(
            @PathVariable Long id) {
        log.info("Get job by id request received: id={}", id);
        JobResponseDto job = jobService.getJobById(id);
        log.info("Get job by id request completed: id={}", id);
        return ResponseEntity.ok(job);
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update job", description = "Updates an existing job. Only the owner recruiter can update.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Job updated successfully",
                    content = @Content(schema = @Schema(implementation = JobResponseDto.class))),
            @ApiResponse(responseCode = "403", description = "Not allowed to update this job"),
            @ApiResponse(responseCode = "404", description = "Job not found")
    })
    public ResponseEntity<JobResponseDto> updateJob(
            @PathVariable Long id,
            @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    description = "Updated job payload",
                    required = true)
            @Valid @RequestBody JobRequestDto dto,
            @RequestHeader("X-User-Id") Long recruiterId) {

        log.info("Update job request received: id={}, recruiterId={}", id, recruiterId);
        JobResponseDto updated = jobService.updateJob(id, dto, recruiterId);
        log.info("Update job request completed: id={}", id);
        return ResponseEntity.ok(updated);
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete job", description = "Deletes a job. Only the owner recruiter can delete.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Job deleted successfully"),
            @ApiResponse(responseCode = "403", description = "Not allowed to delete this job"),
            @ApiResponse(responseCode = "404", description = "Job not found")
    })
    public ResponseEntity<Void> deleteJob(
            @PathVariable Long id,
            @RequestHeader("X-User-Id") Long recruiterId) {

        log.info("Delete job request received: id={}, recruiterId={}", id, recruiterId);
        jobService.deleteJob(id, recruiterId);
        log.info("Delete job request completed: id={}", id);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/search")
    @Operation(summary = "Search jobs", description = "Searches jobs by dynamic filters with pagination and sorting.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Filtered jobs fetched successfully")
    })
    public ResponseEntity<Page<JobResponseDto>> searchJobs(
            @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    description = "Filter payload for job search",
                    required = true)
            @RequestBody JobFilterDto filter,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @RequestParam(defaultValue = "desc") String direction) {

        log.info("Search jobs request received: page={}, size={}, sortBy={}, direction={}", page, size, sortBy, direction);
        Page<JobResponseDto> jobs = jobService.searchJobs(filter, page, size, sortBy, direction);
        log.info("Search jobs request completed: returnedElements={}", jobs.getNumberOfElements());
        return ResponseEntity.ok(jobs);
    }



}
