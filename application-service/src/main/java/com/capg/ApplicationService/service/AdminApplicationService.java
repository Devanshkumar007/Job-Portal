package com.capg.ApplicationService.service;

import com.capg.ApplicationService.dto.ApplicationResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface AdminApplicationService {
    Page<ApplicationResponse> getAllApplications(Long authenticatedUserId, String role, Pageable pageable);

    ApplicationResponse getApplicationById(Long applicationId, Long authenticatedUserId, String role);

    void deleteApplication(Long applicationId, Long authenticatedUserId, String role);
}
