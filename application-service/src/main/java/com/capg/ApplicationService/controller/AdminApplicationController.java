package com.capg.ApplicationService.controller;

import com.capg.ApplicationService.dto.ApplicationResponse;
import com.capg.ApplicationService.service.AdminApplicationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/admin/applications")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Admin Applications")
public class AdminApplicationController {

    private final AdminApplicationService adminApplicationService;

    @GetMapping
    @Operation(summary = "Get all applications (admin)")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Applications fetched"),
            @ApiResponse(responseCode = "403", description = "Only admins can access")
    })
    public ResponseEntity<Page<ApplicationResponse>> getAllApplications(
            @PageableDefault(page = 0, size = 10, sort = "appliedAt", direction = org.springframework.data.domain.Sort.Direction.DESC)
            Pageable pageable,
            @RequestHeader("X-User-Id") Long requesterId,
            @RequestHeader("X-User-Role") String role) {
        log.info("Admin get all applications request: requesterId={}, role={}, page={}, size={}",
                requesterId, role, pageable.getPageNumber(), pageable.getPageSize());
        return ResponseEntity.ok(adminApplicationService.getAllApplications(requesterId, role, pageable));
    }

    @GetMapping("/{applicationId}")
    @Operation(summary = "Get application by id (admin)")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Application fetched"),
            @ApiResponse(responseCode = "403", description = "Only admins can access"),
            @ApiResponse(responseCode = "404", description = "Application not found")
    })
    public ResponseEntity<ApplicationResponse> getApplicationById(
            @PathVariable Long applicationId,
            @RequestHeader("X-User-Id") Long requesterId,
            @RequestHeader("X-User-Role") String role) {
        log.info("Admin get application request: applicationId={}, requesterId={}, role={}",
                applicationId, requesterId, role);
        return ResponseEntity.ok(adminApplicationService.getApplicationById(applicationId, requesterId, role));
    }

    @DeleteMapping("/{applicationId}")
    @Operation(summary = "Delete application by id (admin)")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Application deleted"),
            @ApiResponse(responseCode = "403", description = "Only admins can access"),
            @ApiResponse(responseCode = "404", description = "Application not found")
    })
    public ResponseEntity<Map<String, String>> deleteApplication(
            @PathVariable Long applicationId,
            @RequestHeader("X-User-Id") Long requesterId,
            @RequestHeader("X-User-Role") String role) {
        log.info("Admin delete application request: applicationId={}, requesterId={}, role={}",
                applicationId, requesterId, role);
        adminApplicationService.deleteApplication(applicationId, requesterId, role);
        return ResponseEntity.ok(Map.of("message", "Application deleted successfully"));
    }
}
