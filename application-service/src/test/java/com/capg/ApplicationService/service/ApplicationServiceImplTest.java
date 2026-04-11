package com.capg.ApplicationService.service;

import com.capg.ApplicationService.client.JobClient;
import com.capg.ApplicationService.dto.ApplicationRequest;
import com.capg.ApplicationService.dto.ApplicationResponse;
import com.capg.ApplicationService.dto.JobDetailsResponse;
import com.capg.ApplicationService.dto.StatusUpdateRequest;
import com.capg.ApplicationService.entity.Application;
import com.capg.ApplicationService.entity.ApplicationStatus;
import com.capg.ApplicationService.exception.DuplicateApplicationException;
import com.capg.ApplicationService.exception.InvalidStatusTransitionException;
import com.capg.ApplicationService.exception.ResourceNotFoundException;
import com.capg.ApplicationService.exception.UnauthorizedException;
import com.capg.ApplicationService.repository.ApplicationRepository;
import feign.FeignException;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.modelmapper.ModelMapper;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class ApplicationServiceImplTest {

    @Mock
    private ModelMapper modelMapper;

    @Mock
    private ApplicationRepository repo;

    @Mock
    private JobClient jobClient;

    @Mock
    private CloudinaryService cloudinaryService;

    @Mock
    private EventPublisher eventPublisher;

    @InjectMocks
    private ApplicationServiceImpl service;

    @AfterEach
    void cleanupTxState() {
        if (TransactionSynchronizationManager.isSynchronizationActive()) {
            TransactionSynchronizationManager.clearSynchronization();
        }
        TransactionSynchronizationManager.setActualTransactionActive(false);
    }

    @Test
    @Order(1)
    void apply_success_persists_and_publishes_event() {
        ApplicationRequest request = buildRequest();
        Application mapped = new Application();
        ApplicationResponse mappedResponse = new ApplicationResponse();
        mappedResponse.setUserId(11L);

        when(repo.existsByUserIdAndJobId(11L, 22L)).thenReturn(false);
        when(modelMapper.map(request, Application.class)).thenReturn(mapped);
        when(modelMapper.map(mapped, ApplicationResponse.class)).thenReturn(mappedResponse);

        ApplicationResponse response = service.apply(request, 11L, "JOB_SEEKER");

        assertEquals(11L, mapped.getUserId());
        assertEquals(ApplicationStatus.APPLIED, mapped.getStatus());
        assertNotNull(mapped.getAppliedAt());
        verify(repo).save(mapped);
        verify(eventPublisher).publishApplicationCreatedEvent(any());
        assertEquals(11L, response.getUserId());
    }

    @Test
    @Order(2)
    void apply_duplicate_application_throws_and_skips_save_publish() {
        ApplicationRequest request = buildRequest();
        when(repo.existsByUserIdAndJobId(11L, 22L)).thenReturn(true);

        assertThrows(DuplicateApplicationException.class,
                () -> service.apply(request, 11L, "JOB_SEEKER"));

        verify(repo, never()).save(any());
        verify(eventPublisher, never()).publishApplicationCreatedEvent(any());
    }

    @Test
    @Order(3)
    void apply_non_job_seeker_role_throws_unauthorized() {
        ApplicationRequest request = buildRequest();

        assertThrows(UnauthorizedException.class,
                () -> service.apply(request, 11L, "RECRUITER"));

        verify(repo, never()).existsByUserIdAndJobId(any(), any());
    }

    @Test
    @Order(4)
    void apply_with_active_transaction_registers_after_commit_publish() {
        ApplicationRequest request = buildRequest();
        Application mapped = new Application();
        ApplicationResponse mappedResponse = new ApplicationResponse();

        when(repo.existsByUserIdAndJobId(11L, 22L)).thenReturn(false);
        when(modelMapper.map(request, Application.class)).thenReturn(mapped);
        when(modelMapper.map(mapped, ApplicationResponse.class)).thenReturn(mappedResponse);

        TransactionSynchronizationManager.initSynchronization();
        TransactionSynchronizationManager.setActualTransactionActive(true);

        service.apply(request, 11L, "JOB_SEEKER");

        verify(eventPublisher, never()).publishApplicationCreatedEvent(any());

        List<TransactionSynchronization> synchronizations =
                TransactionSynchronizationManager.getSynchronizations();
        assertEquals(1, synchronizations.size());

        synchronizations.get(0).afterCommit();

        verify(eventPublisher, times(1)).publishApplicationCreatedEvent(any());
    }

    @Test
    @Order(5)
    void getUserApplications_success_when_same_user() {
        Application app = buildApplication(1L, 11L, 22L);
        ApplicationResponse response = new ApplicationResponse();
        response.setId(1L);
        Pageable pageable = PageRequest.of(0, 10);

        when(repo.findByUserId(11L, pageable)).thenReturn(new PageImpl<>(List.of(app), pageable, 1));
        when(modelMapper.map(app, ApplicationResponse.class)).thenReturn(response);

        Page<ApplicationResponse> result =
                service.getUserApplications(11L, 11L, "JOB_SEEKER", pageable);

        assertEquals(1, result.getTotalElements());
        assertEquals(1L, result.getContent().get(0).getId());
    }

    @Test
    @Order(6)
    void getUserApplications_when_user_mismatch_throws_unauthorized() {
        Pageable pageable = PageRequest.of(0, 10);
        assertThrows(UnauthorizedException.class,
                () -> service.getUserApplications(10L, 11L, "JOB_SEEKER", pageable));
        verify(repo, never()).findByUserId(any(), any(Pageable.class));
    }

    @Test
    @Order(7)
    void getJobApplicants_success_for_job_owner() {
        Application app = buildApplication(2L, 99L, 22L);
        ApplicationResponse response = new ApplicationResponse();
        response.setId(2L);
        Pageable pageable = PageRequest.of(0, 10);
        JobDetailsResponse job = new JobDetailsResponse();
        job.setId(22L);
        job.setRecruiterId(77L);

        when(jobClient.getJobById(22L)).thenReturn(job);
        when(repo.findByJobId(22L, pageable)).thenReturn(new PageImpl<>(List.of(app), pageable, 1));
        when(modelMapper.map(app, ApplicationResponse.class)).thenReturn(response);

        Page<ApplicationResponse> result =
                service.getJobApplicants(22L, 77L, "RECRUITER", pageable);

        assertEquals(1, result.getTotalElements());
        assertEquals(2L, result.getContent().get(0).getId());
    }

    @Test
    @Order(8)
    void getJobApplicants_when_role_invalid_throws_unauthorized() {
        Pageable pageable = PageRequest.of(0, 10);
        assertThrows(UnauthorizedException.class,
                () -> service.getJobApplicants(22L, 77L, "JOB_SEEKER", pageable));
        verify(jobClient, never()).getJobById(any());
    }

    @Test
    @Order(9)
    void getJobApplicants_when_not_owner_throws_unauthorized() {
        Pageable pageable = PageRequest.of(0, 10);
        JobDetailsResponse job = new JobDetailsResponse();
        job.setRecruiterId(88L);
        when(jobClient.getJobById(22L)).thenReturn(job);

        assertThrows(UnauthorizedException.class,
                () -> service.getJobApplicants(22L, 77L, "RECRUITER", pageable));

        verify(repo, never()).findByJobId(any(), any(Pageable.class));
    }

    @Test
    @Order(10)
    void getJobApplicants_when_job_not_found_maps_exception() {
        Pageable pageable = PageRequest.of(0, 10);
        doThrow(org.mockito.Mockito.mock(FeignException.NotFound.class))
                .when(jobClient).getJobById(22L);

        assertThrows(ResourceNotFoundException.class,
                () -> service.getJobApplicants(22L, 77L, "RECRUITER", pageable));
    }

    @Test
    @Order(11)
    void getJobApplicants_when_job_client_error_throws_runtime() {
        Pageable pageable = PageRequest.of(0, 10);
        doThrow(org.mockito.Mockito.mock(FeignException.class))
                .when(jobClient).getJobById(22L);

        assertThrows(RuntimeException.class,
                () -> service.getJobApplicants(22L, 77L, "RECRUITER", pageable));
    }

    @Test
    @Order(12)
    void updateStatus_success_updates_and_publishes() {
        StatusUpdateRequest request = new StatusUpdateRequest(5L, "Acme", "SHORTLISTED");
        Application app = buildApplication(5L, 11L, 22L);
        app.setApplicantEmail("applicant@x.com");
        app.setJobTitle("Backend Engineer");
        app.setCompany("Acme");
        ApplicationResponse mappedResponse = new ApplicationResponse();
        mappedResponse.setId(5L);
        JobDetailsResponse job = new JobDetailsResponse();
        job.setRecruiterId(77L);

        when(repo.findById(5L)).thenReturn(Optional.of(app));
        when(jobClient.getJobById(22L)).thenReturn(job);
        when(modelMapper.map(app, ApplicationResponse.class)).thenReturn(mappedResponse);

        ApplicationResponse response = service.updateStatus(request, 77L, "RECRUITER");

        assertEquals(ApplicationStatus.SHORTLISTED, app.getStatus());
        verify(repo).save(app);
        verify(eventPublisher).publishApplicationStausEvent(any());
        assertEquals(5L, response.getId());
    }

    @Test
    @Order(13)
    void updateStatus_when_application_missing_throws_not_found() {
        StatusUpdateRequest request = new StatusUpdateRequest(5L, "Acme", "SHORTLISTED");
        when(repo.findById(5L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class,
                () -> service.updateStatus(request, 77L, "RECRUITER"));
        verify(eventPublisher, never()).publishApplicationStausEvent(any());
    }

    @Test
    @Order(14)
    void updateStatus_when_role_invalid_throws_unauthorized() {
        StatusUpdateRequest request = new StatusUpdateRequest(5L, "Acme", "SHORTLISTED");

        assertThrows(UnauthorizedException.class,
                () -> service.updateStatus(request, 77L, "JOB_SEEKER"));

        verify(repo, never()).findById(any());
    }

    @Test
    @Order(15)
    void updateStatus_when_not_job_owner_throws_unauthorized() {
        StatusUpdateRequest request = new StatusUpdateRequest(5L, "Acme", "SHORTLISTED");
        Application app = buildApplication(5L, 11L, 22L);
        JobDetailsResponse job = new JobDetailsResponse();
        job.setRecruiterId(99L);

        when(repo.findById(5L)).thenReturn(Optional.of(app));
        when(jobClient.getJobById(22L)).thenReturn(job);

        assertThrows(UnauthorizedException.class,
                () -> service.updateStatus(request, 77L, "RECRUITER"));

        verify(repo, never()).save(any());
        verify(eventPublisher, never()).publishApplicationStausEvent(any());
    }

    @Test
    @Order(16)
    void updateStatus_invalid_status_value_throws_illegal_argument() {
        StatusUpdateRequest request = new StatusUpdateRequest(5L, "Acme", "NOT_A_STATUS");
        Application app = buildApplication(5L, 11L, 22L);
        JobDetailsResponse job = new JobDetailsResponse();
        job.setRecruiterId(77L);

        when(repo.findById(5L)).thenReturn(Optional.of(app));
        when(jobClient.getJobById(22L)).thenReturn(job);

        assertThrows(IllegalArgumentException.class,
                () -> service.updateStatus(request, 77L, "RECRUITER"));

        verify(repo, never()).save(any());
        verify(eventPublisher, never()).publishApplicationStausEvent(any());
    }

    @Test
    @Order(17)
    void updateStatus_publish_failure_bubbles_after_commit_edge_case() {
        StatusUpdateRequest request = new StatusUpdateRequest(5L, "Acme", "UNDER_REVIEW");
        Application app = buildApplication(5L, 11L, 22L);
        app.setApplicantEmail("applicant@x.com");
        app.setJobTitle("Backend Engineer");
        app.setCompany("Acme");
        JobDetailsResponse job = new JobDetailsResponse();
        job.setRecruiterId(77L);

        when(repo.findById(5L)).thenReturn(Optional.of(app));
        when(jobClient.getJobById(22L)).thenReturn(job);
        when(modelMapper.map(app, ApplicationResponse.class)).thenReturn(new ApplicationResponse());

        TransactionSynchronizationManager.initSynchronization();
        TransactionSynchronizationManager.setActualTransactionActive(true);
        service.updateStatus(request, 77L, "RECRUITER");

        ArgumentCaptor<com.capg.ApplicationService.dto.ApplicationStatusDto> captor =
                ArgumentCaptor.forClass(com.capg.ApplicationService.dto.ApplicationStatusDto.class);
        doThrow(new RuntimeException("rabbit down"))
                .when(eventPublisher).publishApplicationStausEvent(captor.capture());

        List<TransactionSynchronization> synchronizations =
                TransactionSynchronizationManager.getSynchronizations();
        assertFalse(synchronizations.isEmpty());

        RuntimeException ex = assertThrows(RuntimeException.class,
                () -> synchronizations.forEach(TransactionSynchronization::afterCommit));
        assertEquals("rabbit down", ex.getMessage());
        assertTrue(captor.getValue().getStatus().equals("UNDER_REVIEW"));
    }

    @Test
    @Order(18)
    void updateStatus_interview_scheduled_without_timezone_throws_bad_request() {
        StatusUpdateRequest request = new StatusUpdateRequest(5L, "Acme", "INTERVIEW_SCHEDULED");
        request.setInterviewLink("https://meet.example.com/abc");
        request.setInterviewDate(LocalDate.of(2026, 4, 10));
        request.setInterviewTime(LocalTime.of(14, 0));
        request.setTimeZone(" ");
        Application app = buildApplication(5L, 11L, 22L);
        app.setStatus(ApplicationStatus.SHORTLISTED);
        JobDetailsResponse job = new JobDetailsResponse();
        job.setRecruiterId(77L);

        when(repo.findById(5L)).thenReturn(Optional.of(app));
        when(jobClient.getJobById(22L)).thenReturn(job);

        assertThrows(IllegalArgumentException.class,
                () -> service.updateStatus(request, 77L, "RECRUITER"));

        verify(repo, never()).save(any());
        verify(eventPublisher, never()).publishInterviewScheduledEvent(any());
    }

    @Test
    @Order(19)
    void updateStatus_interview_scheduled_success_publishes_interview_event() {
        StatusUpdateRequest request = new StatusUpdateRequest(5L, "Acme", "INTERVIEW_SCHEDULED");
        request.setInterviewLink("https://meet.example.com/abc");
        request.setInterviewDate(LocalDate.of(2026, 4, 10));
        request.setInterviewTime(LocalTime.of(14, 0));
        request.setTimeZone("Asia/Kolkata");
        Application app = buildApplication(5L, 11L, 22L);
        app.setStatus(ApplicationStatus.SHORTLISTED);
        app.setApplicantEmail("applicant@x.com");
        app.setJobTitle("Backend Engineer");
        app.setCompany("Acme");
        JobDetailsResponse job = new JobDetailsResponse();
        job.setRecruiterId(77L);
        when(repo.findById(5L)).thenReturn(Optional.of(app));
        when(jobClient.getJobById(22L)).thenReturn(job);
        when(modelMapper.map(app, ApplicationResponse.class)).thenReturn(new ApplicationResponse());

        service.updateStatus(request, 77L, "RECRUITER");

        assertEquals(ApplicationStatus.INTERVIEW_SCHEDULED, app.getStatus());
        assertEquals(LocalDate.of(2026, 4, 10), app.getInterviewDate());
        assertEquals(LocalTime.of(14, 0), app.getInterviewTime());
        assertEquals("Asia/Kolkata", app.getInterviewTimeZone());
        verify(eventPublisher, never()).publishApplicationStausEvent(any());
        verify(eventPublisher).publishInterviewScheduledEvent(any());
    }

    @Test
    @Order(20)
    void updateStatus_offered_via_json_endpoint_rejected() {
        StatusUpdateRequest request = new StatusUpdateRequest(5L, "Acme", "OFFERED");
        Application app = buildApplication(5L, 11L, 22L);
        JobDetailsResponse job = new JobDetailsResponse();
        job.setRecruiterId(77L);

        when(repo.findById(5L)).thenReturn(Optional.of(app));
        when(jobClient.getJobById(22L)).thenReturn(job);

        assertThrows(IllegalArgumentException.class,
                () -> service.updateStatus(request, 77L, "RECRUITER"));

        verify(repo, never()).save(any());
    }

    @Test
    @Order(21)
    void updateStatusToOffered_success_uploads_offer_letter_and_updates_status() {
        Application app = buildApplication(5L, 11L, 22L);
        app.setStatus(ApplicationStatus.INTERVIEW_SCHEDULED);
        app.setApplicantEmail("applicant@x.com");
        app.setJobTitle("Backend Engineer");
        app.setCompany("Acme");
        JobDetailsResponse job = new JobDetailsResponse();
        job.setRecruiterId(77L);
        MockMultipartFile file = new MockMultipartFile(
                "file",
                "offer.pdf",
                "application/pdf",
                "dummy".getBytes(StandardCharsets.UTF_8)
        );
        ApplicationResponse response = new ApplicationResponse();
        response.setId(5L);

        when(repo.findById(5L)).thenReturn(Optional.of(app));
        when(jobClient.getJobById(22L)).thenReturn(job);
        when(cloudinaryService.uploadOfferLetter(file)).thenReturn(
                Map.of("url", "https://cdn.example.com/offer.pdf", "publicId", "offer-letters/abc")
        );
        when(modelMapper.map(app, ApplicationResponse.class)).thenReturn(response);

        ApplicationResponse result = service.updateStatusToOffered(5L, "Acme", file, 77L, "RECRUITER");

        assertEquals(ApplicationStatus.OFFERED, app.getStatus());
        assertEquals("https://cdn.example.com/offer.pdf", app.getOfferLetterUrl());
        assertEquals("offer-letters/abc", app.getOfferLetterPublicId());
        assertEquals(5L, result.getId());
        verify(repo).save(app);
        verify(eventPublisher).publishOfferSentEvent(any());
        verify(eventPublisher, never()).publishApplicationStausEvent(any());
    }

    @Test
    @Order(22)
    void updateStatusToOffered_without_interview_scheduled_throws_bad_request() {
        Application app = buildApplication(5L, 11L, 22L);
        app.setStatus(ApplicationStatus.APPLIED);
        JobDetailsResponse job = new JobDetailsResponse();
        job.setRecruiterId(77L);
        MockMultipartFile file = new MockMultipartFile(
                "file",
                "offer.pdf",
                "application/pdf",
                "dummy".getBytes(StandardCharsets.UTF_8)
        );

        when(repo.findById(5L)).thenReturn(Optional.of(app));
        when(jobClient.getJobById(22L)).thenReturn(job);

        assertThrows(InvalidStatusTransitionException.class,
                () -> service.updateStatusToOffered(5L, "Acme", file, 77L, "RECRUITER"));

        verify(cloudinaryService, never()).uploadOfferLetter(any());
        verify(repo, never()).save(any());
        verify(eventPublisher, never()).publishOfferSentEvent(any());
    }

    @Test
    @Order(23)
    void updateStatusToOffered_without_file_throws_bad_request() {
        assertThrows(IllegalArgumentException.class,
                () -> service.updateStatusToOffered(5L, "Acme", null, 77L, "RECRUITER"));

        verify(repo, never()).findById(any());
        verify(eventPublisher, never()).publishOfferSentEvent(any());
    }

    @Test
    @Order(24)
    void updateStatus_applied_to_interview_scheduled_is_rejected_as_skip() {
        StatusUpdateRequest request = new StatusUpdateRequest(5L, "Acme", "INTERVIEW_SCHEDULED");
        request.setInterviewLink("https://meet.example.com/abc");
        request.setInterviewDate(LocalDate.of(2026, 4, 10));
        request.setInterviewTime(LocalTime.of(14, 0));
        request.setTimeZone("Asia/Kolkata");
        Application app = buildApplication(5L, 11L, 22L);
        JobDetailsResponse job = new JobDetailsResponse();
        job.setRecruiterId(77L);

        when(repo.findById(5L)).thenReturn(Optional.of(app));
        when(jobClient.getJobById(22L)).thenReturn(job);

        InvalidStatusTransitionException ex = assertThrows(InvalidStatusTransitionException.class,
                () -> service.updateStatus(request, 77L, "RECRUITER"));

        assertEquals("Invalid status transition from APPLIED to INTERVIEW_SCHEDULED", ex.getMessage());
        verify(repo, never()).save(any());
        verify(eventPublisher, never()).publishInterviewScheduledEvent(any());
    }

    @Test
    @Order(25)
    void updateStatus_under_review_to_applied_is_rejected_as_backward_move() {
        StatusUpdateRequest request = new StatusUpdateRequest(5L, "Acme", "APPLIED");
        Application app = buildApplication(5L, 11L, 22L);
        app.setStatus(ApplicationStatus.UNDER_REVIEW);
        JobDetailsResponse job = new JobDetailsResponse();
        job.setRecruiterId(77L);

        when(repo.findById(5L)).thenReturn(Optional.of(app));
        when(jobClient.getJobById(22L)).thenReturn(job);

        InvalidStatusTransitionException ex = assertThrows(InvalidStatusTransitionException.class,
                () -> service.updateStatus(request, 77L, "RECRUITER"));

        assertEquals("Invalid status transition from UNDER_REVIEW to APPLIED", ex.getMessage());
        verify(repo, never()).save(any());
    }

    @Test
    @Order(26)
    void updateStatus_offered_to_rejected_is_rejected_as_terminal_state() {
        StatusUpdateRequest request = new StatusUpdateRequest(5L, "Acme", "REJECTED");
        Application app = buildApplication(5L, 11L, 22L);
        app.setStatus(ApplicationStatus.OFFERED);
        JobDetailsResponse job = new JobDetailsResponse();
        job.setRecruiterId(77L);

        when(repo.findById(5L)).thenReturn(Optional.of(app));
        when(jobClient.getJobById(22L)).thenReturn(job);

        InvalidStatusTransitionException ex = assertThrows(InvalidStatusTransitionException.class,
                () -> service.updateStatus(request, 77L, "RECRUITER"));

        assertEquals("Application is already in a terminal status: OFFERED", ex.getMessage());
        verify(repo, never()).save(any());
    }

    @Test
    @Order(27)
    void updateStatus_rejected_to_under_review_is_rejected_as_terminal_state() {
        StatusUpdateRequest request = new StatusUpdateRequest(5L, "Acme", "UNDER_REVIEW");
        Application app = buildApplication(5L, 11L, 22L);
        app.setStatus(ApplicationStatus.REJECTED);
        JobDetailsResponse job = new JobDetailsResponse();
        job.setRecruiterId(77L);

        when(repo.findById(5L)).thenReturn(Optional.of(app));
        when(jobClient.getJobById(22L)).thenReturn(job);

        InvalidStatusTransitionException ex = assertThrows(InvalidStatusTransitionException.class,
                () -> service.updateStatus(request, 77L, "RECRUITER"));

        assertEquals("Application is already in a terminal status: REJECTED", ex.getMessage());
        verify(repo, never()).save(any());
    }

    private ApplicationRequest buildRequest() {
        ApplicationRequest request = new ApplicationRequest();
        request.setJobId(22L);
        request.setApplicantEmail("applicant@x.com");
        request.setApplicantName("Test Applicant");
        request.setJobTitle("Backend Engineer");
        request.setCompany("Acme");
        return request;
    }

    private Application buildApplication(Long id, Long userId, Long jobId) {
        Application app = new Application();
        app.setId(id);
        app.setUserId(userId);
        app.setJobId(jobId);
        app.setAppliedAt(LocalDateTime.now());
        app.setStatus(ApplicationStatus.APPLIED);
        return app;
    }
}
