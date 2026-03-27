package com.capg.AdminService.service;

import com.capg.AdminService.dto.ApplicationResponse;

import java.util.List;

public interface AdminApplicationService {
    List<ApplicationResponse> getAllApplications(Long requesterId, String role);

    ApplicationResponse getApplicationById(Long applicationId, Long requesterId, String role);

    void deleteApplication(Long applicationId, Long requesterId, String role);
}
