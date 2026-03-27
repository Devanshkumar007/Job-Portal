package com.capg.AdminService.client;


import com.capg.AdminService.dto.ApplicationResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestHeader;

import java.util.List;

@FeignClient(name="APPLICATION-SERVICE")
public interface ApplicationsClient {

    @GetMapping("/api/admin/applications")
    List<ApplicationResponse> getAllApplications(
            @RequestHeader("X-User-Id") Long requesterId,
            @RequestHeader("X-User-Role") String role);

    @GetMapping("/api/admin/applications/{applicationId}")
    ApplicationResponse getApplicationById(
            @PathVariable Long applicationId,
            @RequestHeader("X-User-Id") Long requesterId,
            @RequestHeader("X-User-Role") String role);

    @DeleteMapping("/api/admin/applications/{applicationId}")
    void deleteApplication(
            @PathVariable Long applicationId,
            @RequestHeader("X-User-Id") Long requesterId,
            @RequestHeader("X-User-Role") String role);

}
