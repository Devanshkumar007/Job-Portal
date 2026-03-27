package com.capg.ApplicationService.controller;

import com.capg.ApplicationService.dto.ApplicationRequest;
import com.capg.ApplicationService.dto.ApplicationResponse;
import com.capg.ApplicationService.dto.StatusUpdateRequest;
import com.capg.ApplicationService.service.ApplicationService;
import com.capg.ApplicationService.service.CloudinaryService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
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
            @RequestParam("jobTitle") String jobTitle,
            @RequestParam("company") String company,
            @RequestHeader("X-User-Id") Long userId,
            @RequestHeader("X-User-Role") String role) {
        log.info("Apply request received: userId={}, role={}, jobId={}", userId, role, jobId);

        Map<String, String> uploadResult = cloudinaryService.uploadResume(resume);

        ApplicationRequest request = new ApplicationRequest();
        request.setJobId(jobId);
        request.setApplicantEmail(applicantEmail);
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
    public ResponseEntity<List<ApplicationResponse>> getUserApps(@PathVariable Long userId,
                                                                 @RequestHeader("X-User-Id") Long requesterId,
                                                                 @RequestHeader("X-User-Role") String role) {
        log.info("Get user applications request: userId={}, requesterId={}, role={}", userId, requesterId, role);
        return ResponseEntity.ok(service.getUserApplications(userId, requesterId, role));
    }

    // GET JOB APPLICATIONS
    @GetMapping("/job/{jobId}")
    @Operation(summary = "Get applicants for a job")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Applicants fetched"),
            @ApiResponse(responseCode = "403", description = "Unauthorized access"),
            @ApiResponse(responseCode = "404", description = "Job not found")
    })
    public ResponseEntity<List<ApplicationResponse>> getJobApps(@PathVariable Long jobId,
                                                                @RequestHeader("X-User-Id") Long requesterId,
                                                                @RequestHeader("X-User-Role") String role) {
        log.info("Get job applicants request: jobId={}, requesterId={}, role={}", jobId, requesterId, role);
        return ResponseEntity.ok(service.getJobApplicants(jobId, requesterId, role));
    }

    // UPDATE STATUS
    @PutMapping("/status")
    @Operation(summary = "Update application status")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Status updated"),
            @ApiResponse(responseCode = "400", description = "Invalid status"),
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
}
