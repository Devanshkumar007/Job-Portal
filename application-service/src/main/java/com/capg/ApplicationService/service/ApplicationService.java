package com.capg.ApplicationService.service;

import com.capg.ApplicationService.dto.ApplicationRequest;
import com.capg.ApplicationService.dto.ApplicationResponse;
import com.capg.ApplicationService.dto.StatusUpdateRequest;
import java.util.List;

public interface ApplicationService {
    ApplicationResponse apply(ApplicationRequest request, Long authenticatedUserId, String role);

    List<ApplicationResponse> getUserApplications(Long requestedUserId,
                                                  Long authenticatedUserId,
                                                  String role);

    List<ApplicationResponse> getJobApplicants(Long jobId,
                                               Long authenticatedUserId,
                                               String role);

    ApplicationResponse updateStatus(StatusUpdateRequest request,
                                     Long authenticatedUserId,
                                     String role);
}



