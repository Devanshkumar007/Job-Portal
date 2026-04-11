package com.capg.ApplicationService.service;

import com.capg.ApplicationService.dto.ApplicationRequest;
import com.capg.ApplicationService.dto.ApplicationResponse;
import com.capg.ApplicationService.dto.StatusUpdateRequest;
import com.capg.ApplicationService.entity.ApplicationStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.web.multipart.MultipartFile;

public interface ApplicationService {
    ApplicationResponse apply(ApplicationRequest request, Long authenticatedUserId, String role);

    Page<ApplicationResponse> getUserApplications(Long requestedUserId,
                                                  Long authenticatedUserId,
                                                  String role,
                                                  Pageable pageable);

    Page<ApplicationResponse> getUserApplicationsByStatus(Long authenticatedUserId,
                                                          String role,
                                                          ApplicationStatus status,
                                                          Pageable pageable);

    Page<ApplicationResponse> getJobApplicants(Long jobId,
                                               Long authenticatedUserId,
                                               String role,
                                               Pageable pageable);

    ApplicationResponse updateStatus(StatusUpdateRequest request,
                                     Long authenticatedUserId,
                                     String role);

    ApplicationResponse updateStatusToOffered(Long applicationId,
                                              String company,
                                              MultipartFile offerLetterFile,
                                              Long authenticatedUserId,
                                              String role);
}

