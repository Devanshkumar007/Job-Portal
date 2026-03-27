package com.capg.AdminService.controller;

import com.capg.AdminService.dto.ApplicationResponse;
import com.capg.AdminService.service.AdminApplicationService;
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
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/admin/applications")
@RequiredArgsConstructor
@Tag(name = "Admin Applications", description = "Admin operations for applications")
@Slf4j
public class AdminApplicationController {

    private final AdminApplicationService adminApplicationService;

    @GetMapping
    @Operation(summary = "Get all applications", description = "Fetch all job applications for admin users")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Applications fetched successfully"),
            @ApiResponse(responseCode = "403", description = "Only admins can access this resource"),
            @ApiResponse(responseCode = "503", description = "Application service unreachable")
    })
    public ResponseEntity<List<ApplicationResponse>> getAllApplications(
            @RequestHeader("X-User-Id") Long requesterId,
            @RequestHeader("X-User-Role") String role) {
        log.info("Admin applications request: action=getAllApplications, requesterId={}, role={}", requesterId, role);
        return ResponseEntity.ok(adminApplicationService.getAllApplications(requesterId, role));
    }

    @GetMapping("/{applicationId}")
    @Operation(summary = "Get application by id", description = "Fetch a single application by id")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Application fetched successfully"),
            @ApiResponse(responseCode = "403", description = "Only admins can access this resource"),
            @ApiResponse(responseCode = "503", description = "Application service unreachable")
    })
    public ResponseEntity<ApplicationResponse> getApplicationById(
            @PathVariable Long applicationId,
            @RequestHeader("X-User-Id") Long requesterId,
            @RequestHeader("X-User-Role") String role) {
        log.info(
                "Admin applications request: action=getApplicationById, applicationId={}, requesterId={}, role={}",
                applicationId, requesterId, role
        );
        return ResponseEntity.ok(adminApplicationService.getApplicationById(applicationId, requesterId, role));
    }

    @DeleteMapping("/{applicationId}")
    @Operation(summary = "Delete application by id", description = "Delete an application by id")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Application deleted successfully"),
            @ApiResponse(responseCode = "403", description = "Only admins can access this resource"),
            @ApiResponse(responseCode = "503", description = "Application service unreachable")
    })
    public ResponseEntity<Map<String, String>> deleteApplication(
            @PathVariable Long applicationId,
            @RequestHeader("X-User-Id") Long requesterId,
            @RequestHeader("X-User-Role") String role) {
        log.info(
                "Admin applications request: action=deleteApplication, applicationId={}, requesterId={}, role={}",
                applicationId, requesterId, role
        );
        adminApplicationService.deleteApplication(applicationId, requesterId, role);
        return ResponseEntity.ok(Map.of("message", "Application deleted successfully"));
    }
}
