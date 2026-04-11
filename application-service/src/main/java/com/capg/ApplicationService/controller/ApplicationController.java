package com.capg.ApplicationService.controller;

import com.capg.ApplicationService.dto.ApplicationRequest;
import com.capg.ApplicationService.dto.ApplicationResponse;
import com.capg.ApplicationService.dto.StatusUpdateRequest;
import com.capg.ApplicationService.entity.ApplicationStatus;
import com.capg.ApplicationService.service.ApplicationService;
import com.capg.ApplicationService.service.CloudinaryService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.Map;

@RestController
@RequestMapping("/api/applications")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Applications")
public class ApplicationController {

    private final ApplicationService service;
    private final CloudinaryService cloudinaryService;

//    // APPLY
//    @PostMapping(value = "/apply", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
//    public ResponseEntity<ApplicationResponse> apply(
//            @RequestPart("file") MultipartFile resume,
//            @RequestPart("data") ApplicationRequest request,
//            @RequestHeader("X-User-Id") Long userId,
//            @RequestHeader("X-User-Role") String role) {
//
//        String resumeUrl = cloudinaryService.uploadResume(resume);
//        request.setResumeUrl(resumeUrl);
//
//        ApplicationResponse response = service.apply(request, userId, role);
//        return ResponseEntity.ok(response);
//    }

    @PostMapping(value = "/apply", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @Operation(summary = "Apply for a job")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Application submitted"),
            @ApiResponse(responseCode = "400", description = "Invalid request or duplicate application"),
            @ApiResponse(responseCode = "403", description = "Unauthorized role")
    })
    public ResponseEntity<ApplicationResponse> apply(
            @RequestPart("file") MultipartFile resume,
            @RequestParam("jobId") Long jobId,
            @RequestParam("applicantEmail") String applicantEmail,
            @RequestParam("applicantName") String applicantName,
            @RequestParam("jobTitle") String jobTitle,
            @RequestParam("company") String company,
            @RequestHeader("X-User-Id") Long userId,
            @RequestHeader("X-User-Role") String role) {
        log.info("Apply request received: userId={}, role={}, jobId={}", userId, role, jobId);

        Map<String, String> uploadResult = cloudinaryService.uploadResume(resume);

        ApplicationRequest request = new ApplicationRequest();
        request.setJobId(jobId);
        request.setApplicantEmail(applicantEmail);
        request.setApplicantName(applicantName);
        request.setJobTitle(jobTitle);
        request.setCompany(company);
        request.setResumeUrl(uploadResult.get("url"));
        request.setResumePublicId(uploadResult.get("publicId"));

        return ResponseEntity.ok(service.apply(request, userId, role));
    }


    // GET USER APPLICATIONS
    @GetMapping("/user/{userId}")
    @Operation(summary = "Get applications for a user")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Applications fetched"),
            @ApiResponse(responseCode = "403", description = "Unauthorized access")
    })
    public ResponseEntity<Page<ApplicationResponse>> getUserApps(@PathVariable Long userId,
                                                                 @PageableDefault(page = 0, size = 10, sort = "appliedAt", direction = Sort.Direction.DESC)
                                                                 Pageable pageable,
                                                                 @RequestHeader("X-User-Id") Long requesterId,
                                                                 @RequestHeader("X-User-Role") String role) {
        log.info("Get user applications request: userId={}, requesterId={}, role={}, page={}, size={}",
                userId, requesterId, role, pageable.getPageNumber(), pageable.getPageSize());
        return ResponseEntity.ok(service.getUserApplications(userId, requesterId, role, pageable));
    }

    @GetMapping("/user/by-status")
    @Operation(summary = "Get applications for authenticated user filtered by status")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Applications fetched"),
            @ApiResponse(responseCode = "400", description = "Invalid status"),
            @ApiResponse(responseCode = "403", description = "Unauthorized access")
    })
    public ResponseEntity<Page<ApplicationResponse>> getUserAppsByStatus(
            @RequestParam("status") String status,
            @PageableDefault(page = 0, size = 10, sort = "appliedAt", direction = Sort.Direction.DESC)
            Pageable pageable,
            @RequestHeader("X-User-Id") Long userId,
            @RequestHeader("X-User-Role") String role) {
        log.info("Get user applications by status request: userId={}, role={}, status={}, page={}, size={}",
                userId, role, status, pageable.getPageNumber(), pageable.getPageSize());

        ApplicationStatus parsedStatus;
        try {
            parsedStatus = ApplicationStatus.valueOf(status.trim().toUpperCase());
        } catch (Exception ex) {
            throw new IllegalArgumentException("Invalid status: " + status);
        }

        return ResponseEntity.ok(service.getUserApplicationsByStatus(userId, role, parsedStatus, pageable));
    }

    // GET JOB APPLICATIONS
    @GetMapping("/job/{jobId}")
    @Operation(summary = "Get applicants for a job")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Applicants fetched"),
            @ApiResponse(responseCode = "403", description = "Unauthorized access"),
            @ApiResponse(responseCode = "404", description = "Job not found")
    })
    public ResponseEntity<Page<ApplicationResponse>> getJobApps(@PathVariable Long jobId,
                                                                @PageableDefault(page = 0, size = 10, sort = "appliedAt", direction = Sort.Direction.DESC)
                                                                Pageable pageable,
                                                                @RequestHeader("X-User-Id") Long requesterId,
                                                                @RequestHeader("X-User-Role") String role) {
        log.info("Get job applicants request: jobId={}, requesterId={}, role={}, page={}, size={}",
                jobId, requesterId, role, pageable.getPageNumber(), pageable.getPageSize());
        return ResponseEntity.ok(service.getJobApplicants(jobId, requesterId, role, pageable));
    }

    // UPDATE STATUS
    @PutMapping("/status")
    @Operation(summary = "Update application status")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Status updated"),
            @ApiResponse(responseCode = "400", description = "Invalid status"),
            @ApiResponse(responseCode = "409", description = "Invalid status transition"),
            @ApiResponse(responseCode = "403", description = "Unauthorized access"),
            @ApiResponse(responseCode = "404", description = "Application or job not found")
    })
    public ResponseEntity<ApplicationResponse> updateStatus(@RequestBody StatusUpdateRequest request,
                                                            @RequestHeader("X-User-Id") Long requesterId,
                                                            @RequestHeader("X-User-Role") String role) {
        log.info("Update status request: applicationId={}, requesterId={}, role={}",
                request.getApplicationId(), requesterId, role);
        return ResponseEntity.ok(service.updateStatus(request, requesterId, role));
    }

    @PutMapping(value = "/status/offered", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @Operation(summary = "Update application status to OFFERED with offer letter PDF")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Status updated to offered"),
            @ApiResponse(responseCode = "400", description = "Offer letter PDF missing or invalid"),
            @ApiResponse(responseCode = "409", description = "Invalid status transition"),
            @ApiResponse(responseCode = "403", description = "Unauthorized access"),
            @ApiResponse(responseCode = "404", description = "Application or job not found")
    })
    public ResponseEntity<ApplicationResponse> updateStatusToOffered(
            @RequestParam("applicationId") Long applicationId,
            @RequestParam(value = "company", required = false) String company,
            @RequestPart("file") MultipartFile offerLetterFile,
            @RequestHeader("X-User-Id") Long requesterId,
            @RequestHeader("X-User-Role") String role) {
        log.info("Update status to offered request: applicationId={}, requesterId={}, role={}",
                applicationId, requesterId, role);
        return ResponseEntity.ok(
                service.updateStatusToOffered(applicationId, company, offerLetterFile, requesterId, role)
        );
    }
}
