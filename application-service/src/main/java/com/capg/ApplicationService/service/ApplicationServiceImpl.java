package com.capg.ApplicationService.service;
import com.capg.ApplicationService.client.JobClient;
import com.capg.ApplicationService.dto.*;
import com.capg.ApplicationService.entity.Application;
import com.capg.ApplicationService.entity.ApplicationStatus;
import com.capg.ApplicationService.exception.DuplicateApplicationException;
import com.capg.ApplicationService.exception.InvalidStatusTransitionException;
import com.capg.ApplicationService.exception.ResourceNotFoundException;
import com.capg.ApplicationService.exception.UnauthorizedException;
import com.capg.ApplicationService.repository.ApplicationRepository;
import feign.FeignException;
import lombok.AllArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.CachePut;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.annotation.Caching;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import java.time.LocalDateTime;
import java.util.EnumSet;
import java.util.Map;
import java.util.Set;
import org.springframework.web.multipart.MultipartFile;

@Service
@AllArgsConstructor
public class ApplicationServiceImpl implements ApplicationService{

    private static final String ROLE_JOB_SEEKER = "JOB_SEEKER";
    private static final String ROLE_RECRUITER = "RECRUITER";
    private static final Map<ApplicationStatus, Set<ApplicationStatus>> ALLOWED_STATUS_TRANSITIONS = Map.of(
            ApplicationStatus.APPLIED, EnumSet.of(ApplicationStatus.UNDER_REVIEW, ApplicationStatus.SHORTLISTED, ApplicationStatus.REJECTED),
            ApplicationStatus.UNDER_REVIEW, EnumSet.of(ApplicationStatus.SHORTLISTED, ApplicationStatus.REJECTED),
            ApplicationStatus.SHORTLISTED, EnumSet.of(ApplicationStatus.INTERVIEW_SCHEDULED, ApplicationStatus.REJECTED),
            ApplicationStatus.INTERVIEW_SCHEDULED, EnumSet.of(ApplicationStatus.OFFERED, ApplicationStatus.REJECTED),
            ApplicationStatus.OFFERED, EnumSet.noneOf(ApplicationStatus.class),
            ApplicationStatus.REJECTED, EnumSet.noneOf(ApplicationStatus.class)
    );

    private ModelMapper modelMapper;
    private ApplicationRepository repo;
    private JobClient jobClient;
    private CloudinaryService cloudinaryService;
    private final EventPublisher eventPublisher;

    @Override
    @Transactional
    @CacheEvict(
            value = {"userApplications", "jobApplicants", "adminAllApplications", "adminApplicationById"},
            allEntries = true
    )
    public ApplicationResponse apply(ApplicationRequest request,
                                     Long authenticatedUserId,
                                     String role) {
        assertRole(role, ROLE_JOB_SEEKER,
                "Only job seekers can apply for jobs");

        if(repo.existsByUserIdAndJobId(authenticatedUserId, request.getJobId())){
            throw new DuplicateApplicationException("you already applied!");
        }
        Application app = modelMapper.map(request, Application.class);
        app.setUserId(authenticatedUserId);
        app.setStatus(ApplicationStatus.APPLIED);
        app.setAppliedAt(LocalDateTime.now());
        //app.setResumeUrl(request.getResumeUrl());  // explicitly set AFTER mapping


        repo.save(app);

        ApplicationCreatedDto event =
                new ApplicationCreatedDto(
                        request.getApplicantEmail(),
                        request.getJobTitle(),
                        request.getCompany(),
                        "APPLIED"
                );

        publishAfterCommit(() -> eventPublisher.publishApplicationCreatedEvent(event));

        return modelMapper.map(app,ApplicationResponse.class);
    }

    @Override
    @Cacheable(value = "userApplications",
            key = "#requestedUserId + ':' + #authenticatedUserId + ':' + #role + ':' + #pageable.pageNumber + ':' + #pageable.pageSize + ':' + #pageable.sort")
    public Page<ApplicationResponse> getUserApplications(Long requestedUserId,
                                                         Long authenticatedUserId,
                                                         String role,
                                                         Pageable pageable) {
        assertRole(role, ROLE_JOB_SEEKER,
                "Only job seekers can view applications by userId");

        if (!requestedUserId.equals(authenticatedUserId)) {
            throw new UnauthorizedException(
                    "You can only access your own applications");
        }

        return repo.findByUserId(requestedUserId, pageable)
                .map(app -> modelMapper.map(app, ApplicationResponse.class));
    }

    @Override
    @Cacheable(value = "userApplications",
            key = "#authenticatedUserId + ':' + #role + ':' + #status + ':' + #pageable.pageNumber + ':' + #pageable.pageSize + ':' + #pageable.sort")
    public Page<ApplicationResponse> getUserApplicationsByStatus(Long authenticatedUserId,
                                                                 String role,
                                                                 ApplicationStatus status,
                                                                 Pageable pageable) {
        assertRole(role, ROLE_JOB_SEEKER,
                "Only job seekers can view their applications");

        return repo.findByUserIdAndStatus(authenticatedUserId, status, pageable)
                .map(app -> modelMapper.map(app, ApplicationResponse.class));
    }

    @Override
    @Cacheable(value = "jobApplicants",
            key = "#jobId + ':' + #authenticatedUserId + ':' + #role + ':' + #pageable.pageNumber + ':' + #pageable.pageSize + ':' + #pageable.sort")
    public Page<ApplicationResponse> getJobApplicants(Long jobId,
                                                      Long authenticatedUserId,
                                                      String role,
                                                      Pageable pageable) {
        assertRole(role, ROLE_RECRUITER,
                "Only recruiters can view applicants by jobId");

        JobDetailsResponse job = fetchJob(jobId);
        if (!authenticatedUserId.equals(job.getRecruiterId())) {
            throw new UnauthorizedException(
                    "You can only access applicants for your own posted job");
        }

        return repo.findByJobId(jobId, pageable)
                .map(app -> modelMapper.map(app, ApplicationResponse.class));
    }

    @Override
    @Transactional
    @Caching(
            put = {
                    @CachePut(value = "adminApplicationById", key = "#request.applicationId + ':ADMIN'")
            },
            evict = {
                    @CacheEvict(value = {"userApplications", "jobApplicants", "adminAllApplications"}, allEntries = true)
            }
    )
    public ApplicationResponse updateStatus(StatusUpdateRequest request,
                                            Long authenticatedUserId,
                                            String role) {
        assertRole(role, ROLE_RECRUITER,
                "Only recruiters can update application status");

        Application app = repo.findById(request.getApplicationId())
                .orElseThrow(()-> new ResourceNotFoundException("Not found"));

        JobDetailsResponse job = fetchJob(app.getJobId());
        if (!authenticatedUserId.equals(job.getRecruiterId())) {
            throw new UnauthorizedException(
                    "You can only update status for applications on your own jobs");
        }

        ApplicationStatus newStatus = parseStatus(request.getStatus());

        if (newStatus == ApplicationStatus.OFFERED) {
            throw new IllegalArgumentException(
                    "Use /api/applications/status/offered endpoint to upload offer letter PDF");
        }
        validateStatusTransition(app.getStatus(), newStatus);

        if (newStatus == ApplicationStatus.INTERVIEW_SCHEDULED) {
            validateInterviewDetails(request);
            app.setInterviewLink(request.getInterviewLink());
            app.setInterviewDate(request.getInterviewDate());
            app.setInterviewTime(request.getInterviewTime());
            app.setInterviewTimeZone(request.getTimeZone());
        }

        app.setStatus(newStatus);
        repo.save(app);

        if (newStatus == ApplicationStatus.INTERVIEW_SCHEDULED) {
            InterviewScheduledDto interviewEvent = new InterviewScheduledDto(
                    app.getApplicantEmail(),
                    app.getApplicantName(),
                    app.getJobTitle(),
                    app.getCompany(),
                    request.getInterviewLink(),
                    request.getInterviewDate(),
                    request.getInterviewTime(),
                    request.getTimeZone()
            );
            publishAfterCommit(() -> eventPublisher.publishInterviewScheduledEvent(interviewEvent));
        } else {
            ApplicationStatusDto event =
                    new ApplicationStatusDto(
                            app.getApplicantEmail(),
                            app.getJobTitle(),
                            newStatus.name(),
                            app.getCompany()
                    );
            publishAfterCommit(() -> eventPublisher.publishApplicationStausEvent(event));
        }


        return modelMapper.map(app, ApplicationResponse.class);
    }

    @Override
    @Transactional
    @Caching(
            put = {
                    @CachePut(value = "adminApplicationById", key = "#applicationId + ':ADMIN'")
            },
            evict = {
                    @CacheEvict(value = {"userApplications", "jobApplicants", "adminAllApplications"}, allEntries = true)
            }
    )
    public ApplicationResponse updateStatusToOffered(Long applicationId,
                                                     String company,
                                                     MultipartFile offerLetterFile,
                                                     Long authenticatedUserId,
                                                     String role) {
        assertRole(role, ROLE_RECRUITER,
                "Only recruiters can update application status");

        if (offerLetterFile == null || offerLetterFile.isEmpty()) {
            throw new IllegalArgumentException("Offer letter PDF is required");
        }

        Application app = repo.findById(applicationId)
                .orElseThrow(() -> new ResourceNotFoundException("Not found"));

        JobDetailsResponse job = fetchJob(app.getJobId());
        if (!authenticatedUserId.equals(job.getRecruiterId())) {
            throw new UnauthorizedException(
                    "You can only update status for applications on your own jobs");
        }
        validateStatusTransition(app.getStatus(), ApplicationStatus.OFFERED);

        Map<String, String> uploadResult = cloudinaryService.uploadOfferLetter(offerLetterFile);
        app.setOfferLetterUrl(uploadResult.get("url"));
        app.setOfferLetterPublicId(uploadResult.get("publicId"));
        app.setStatus(ApplicationStatus.OFFERED);
        repo.save(app);

        OfferSentDto offerSentEvent = new OfferSentDto(
                app.getId(),
                app.getApplicantEmail(),
                app.getApplicantName(),
                app.getJobTitle(),
                app.getCompany(),
                ApplicationStatus.OFFERED.name(),
                app.getOfferLetterUrl()
        );

        publishAfterCommit(() -> eventPublisher.publishOfferSentEvent(offerSentEvent));

        return modelMapper.map(app, ApplicationResponse.class);
    }

    private void assertRole(String actualRole, String expectedRole, String message) {
        if (!expectedRole.equalsIgnoreCase(actualRole)) {
            throw new UnauthorizedException(message);
        }
    }

    private JobDetailsResponse fetchJob(Long jobId) {
        try {
            return jobClient.getJobById(jobId);
        } catch (FeignException.NotFound e) {
            throw new ResourceNotFoundException("Job not found with id: " + jobId);
        } catch (FeignException e) {
            throw new RuntimeException("Unable to validate job ownership");
        }
    }

    private ApplicationStatus parseStatus(String status) {
        try {
            return ApplicationStatus.valueOf(status.trim().toUpperCase());
        } catch (Exception ex) {
            throw new IllegalArgumentException("Invalid status: " + status);
        }
    }

    private void validateInterviewDetails(StatusUpdateRequest request) {
        if (isBlank(request.getInterviewLink())) {
            throw new IllegalArgumentException("Interview link is required for INTERVIEW_SCHEDULED status");
        }
        if (request.getInterviewDate() == null) {
            throw new IllegalArgumentException("Interview date is required for INTERVIEW_SCHEDULED status");
        }
        if (request.getInterviewTime() == null) {
            throw new IllegalArgumentException("Interview time is required for INTERVIEW_SCHEDULED status");
        }
        if (isBlank(request.getTimeZone())) {
            throw new IllegalArgumentException("Time zone is required for INTERVIEW_SCHEDULED status");
        }
    }

    private boolean isBlank(String value) {
        return value == null || value.trim().isEmpty();
    }

    private void validateStatusTransition(ApplicationStatus currentStatus, ApplicationStatus newStatus) {
        if (currentStatus == ApplicationStatus.OFFERED || currentStatus == ApplicationStatus.REJECTED) {
            throw new InvalidStatusTransitionException(
                    "Application is already in a terminal status: " + currentStatus);
        }

        Set<ApplicationStatus> allowedNextStatuses =
                ALLOWED_STATUS_TRANSITIONS.getOrDefault(currentStatus, Set.of());

        if (!allowedNextStatuses.contains(newStatus)) {
            throw new InvalidStatusTransitionException(
                    "Invalid status transition from " + currentStatus + " to " + newStatus);
        }
    }

    private void publishAfterCommit(Runnable publishAction) {
        if (!TransactionSynchronizationManager.isActualTransactionActive()) {
            publishAction.run();
            return;
        }

        TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
            @Override
            public void afterCommit() {
                publishAction.run();
            }
        });
    }
}
