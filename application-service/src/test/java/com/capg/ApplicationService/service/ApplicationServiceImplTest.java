package com.capg.ApplicationService.service;

import com.capg.ApplicationService.client.JobClient;
import com.capg.ApplicationService.dto.ApplicationRequest;
import com.capg.ApplicationService.dto.ApplicationResponse;
import com.capg.ApplicationService.dto.JobDetailsResponse;
import com.capg.ApplicationService.dto.StatusUpdateRequest;
import com.capg.ApplicationService.entity.Application;
import com.capg.ApplicationService.entity.ApplicationStatus;
import com.capg.ApplicationService.exception.DuplicateApplicationException;
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
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import java.time.LocalDateTime;
import java.util.List;
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

        when(repo.findByUserId(11L)).thenReturn(List.of(app));
        when(modelMapper.map(app, ApplicationResponse.class)).thenReturn(response);

        List<ApplicationResponse> result =
                service.getUserApplications(11L, 11L, "JOB_SEEKER");

        assertEquals(1, result.size());
        assertEquals(1L, result.get(0).getId());
    }

    @Test
    @Order(6)
    void getUserApplications_when_user_mismatch_throws_unauthorized() {
        assertThrows(UnauthorizedException.class,
                () -> service.getUserApplications(10L, 11L, "JOB_SEEKER"));
        verify(repo, never()).findByUserId(any());
    }

    @Test
    @Order(7)
    void getJobApplicants_success_for_job_owner() {
        Application app = buildApplication(2L, 99L, 22L);
        ApplicationResponse response = new ApplicationResponse();
        response.setId(2L);
        JobDetailsResponse job = new JobDetailsResponse();
        job.setId(22L);
        job.setRecruiterId(77L);

        when(jobClient.getJobById(22L)).thenReturn(job);
        when(repo.findByJobId(22L)).thenReturn(List.of(app));
        when(modelMapper.map(app, ApplicationResponse.class)).thenReturn(response);

        List<ApplicationResponse> result =
                service.getJobApplicants(22L, 77L, "RECRUITER");

        assertEquals(1, result.size());
        assertEquals(2L, result.get(0).getId());
    }

    @Test
    @Order(8)
    void getJobApplicants_when_role_invalid_throws_unauthorized() {
        assertThrows(UnauthorizedException.class,
                () -> service.getJobApplicants(22L, 77L, "JOB_SEEKER"));
        verify(jobClient, never()).getJobById(any());
    }

    @Test
    @Order(9)
    void getJobApplicants_when_not_owner_throws_unauthorized() {
        JobDetailsResponse job = new JobDetailsResponse();
        job.setRecruiterId(88L);
        when(jobClient.getJobById(22L)).thenReturn(job);

        assertThrows(UnauthorizedException.class,
                () -> service.getJobApplicants(22L, 77L, "RECRUITER"));

        verify(repo, never()).findByJobId(any());
    }

    @Test
    @Order(10)
    void getJobApplicants_when_job_not_found_maps_exception() {
        doThrow(org.mockito.Mockito.mock(FeignException.NotFound.class))
                .when(jobClient).getJobById(22L);

        assertThrows(ResourceNotFoundException.class,
                () -> service.getJobApplicants(22L, 77L, "RECRUITER"));
    }

    @Test
    @Order(11)
    void getJobApplicants_when_job_client_error_throws_runtime() {
        doThrow(org.mockito.Mockito.mock(FeignException.class))
                .when(jobClient).getJobById(22L);

        assertThrows(RuntimeException.class,
                () -> service.getJobApplicants(22L, 77L, "RECRUITER"));
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

    private ApplicationRequest buildRequest() {
        ApplicationRequest request = new ApplicationRequest();
        request.setJobId(22L);
        request.setApplicantEmail("applicant@x.com");
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
