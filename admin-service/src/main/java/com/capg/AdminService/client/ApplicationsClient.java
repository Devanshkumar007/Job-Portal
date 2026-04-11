package com.capg.AdminService.client;


import com.capg.AdminService.dto.ApplicationResponse;
import com.capg.AdminService.dto.PagedResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestParam;

@FeignClient(name="APPLICATION-SERVICE")
public interface ApplicationsClient {

    @GetMapping("/api/admin/applications")
    PagedResponse<ApplicationResponse> getAllApplications(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "appliedAt,desc") String sort,
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
