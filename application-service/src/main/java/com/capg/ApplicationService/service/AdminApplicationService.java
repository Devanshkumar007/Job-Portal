package com.capg.ApplicationService.service;

import com.capg.ApplicationService.dto.ApplicationResponse;

import java.util.List;

public interface AdminApplicationService {
    List<ApplicationResponse> getAllApplications(Long authenticatedUserId, String role);

    ApplicationResponse getApplicationById(Long applicationId, Long authenticatedUserId, String role);

    void deleteApplication(Long applicationId, Long authenticatedUserId, String role);
}
