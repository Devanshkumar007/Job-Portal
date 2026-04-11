package com.capg.AdminService.service;

import com.capg.AdminService.dto.ApplicationResponse;
import com.capg.AdminService.dto.PagedResponse;

public interface AdminApplicationService {
    PagedResponse<ApplicationResponse> getAllApplications(
            Long requesterId,
            String role,
            int page,
            int size,
            String sortBy,
            String direction);

    ApplicationResponse getApplicationById(Long applicationId, Long requesterId, String role);

    void deleteApplication(Long applicationId, Long requesterId, String role);
}
