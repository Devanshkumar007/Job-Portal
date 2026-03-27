package com.capg.ApplicationService.service;
import com.capg.ApplicationService.client.JobClient;
import com.capg.ApplicationService.dto.*;
import com.capg.ApplicationService.entity.Application;
import com.capg.ApplicationService.entity.ApplicationStatus;
import com.capg.ApplicationService.exception.DuplicateApplicationException;
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
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import java.time.LocalDateTime;
import java.util.List;

@Service
@AllArgsConstructor
public class ApplicationServiceImpl implements ApplicationService{

    private static final String ROLE_JOB_SEEKER = "JOB_SEEKER";
    private static final String ROLE_RECRUITER = "RECRUITER";

    private ModelMapper modelMapper;
    private ApplicationRepository repo;
    private JobClient jobClient;
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
    @Cacheable(value = "userApplications", key = "#requestedUserId + ':' + #authenticatedUserId + ':' + #role")
    public List<ApplicationResponse> getUserApplications(Long requestedUserId,
                                                         Long authenticatedUserId,
                                                         String role) {
        assertRole(role, ROLE_JOB_SEEKER,
                "Only job seekers can view applications by userId");

        if (!requestedUserId.equals(authenticatedUserId)) {
            throw new UnauthorizedException(
                    "You can only access your own applications");
        }

        return repo.findByUserId(requestedUserId)
                .stream()
                .map(app -> modelMapper.map(app, ApplicationResponse.class))
                .toList();
    }

    @Override
    @Cacheable(value = "jobApplicants", key = "#jobId + ':' + #authenticatedUserId + ':' + #role")
    public List<ApplicationResponse> getJobApplicants(Long jobId,
                                                      Long authenticatedUserId,
                                                      String role) {
        assertRole(role, ROLE_RECRUITER,
                "Only recruiters can view applicants by jobId");

        JobDetailsResponse job = fetchJob(jobId);
        if (!authenticatedUserId.equals(job.getRecruiterId())) {
            throw new UnauthorizedException(
                    "You can only access applicants for your own posted job");
        }

        return repo.findByJobId(jobId)
                .stream()
                .map(app -> modelMapper.map(app, ApplicationResponse.class))
                .toList();
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

        app.setStatus(ApplicationStatus.valueOf(request.getStatus()));
        repo.save(app);

        ApplicationStatusDto event =
                new ApplicationStatusDto(
                        app.getApplicantEmail(),
                        app.getJobTitle(),
                        request.getStatus(),
                        app.getCompany()
                );

        publishAfterCommit(() -> eventPublisher.publishApplicationStausEvent(event));



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
