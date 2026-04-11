package com.capg.JobService.service;

import com.capg.JobService.dto.JobCreatedDto;
import com.capg.JobService.dto.JobDeletedEvent;
import com.capg.JobService.dto.JobFilterDto;
import com.capg.JobService.dto.JobRequestDto;
import com.capg.JobService.dto.JobResponseDto;
import com.capg.JobService.dto.RecruiterJobSummaryDto;
import com.capg.JobService.dto.RecruiterOpenRolesCountDto;
import com.capg.JobService.dto.RecruiterRecentJobDto;
import com.capg.JobService.entity.Job;
import com.capg.JobService.entity.JobStatus;
import com.capg.JobService.exceptions.JobNotFoundException;
import com.capg.JobService.exceptions.UnauthorizedException;
import com.capg.JobService.repository.JobRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
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
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class JobServiceImplTest {

    @Mock
    private JobRepository jobRepository;

    @Mock
    private ModelMapper modelMapper;

    @Mock
    private EventPublisher eventPublisher;

    @InjectMocks
    private JobServiceImpl jobService;

    @Test
    @DisplayName("createJob should save and publish event for recruiter role")
    void createJob_success() {
        JobRequestDto request = buildRequest();
        Job mappedJob = new Job();
        Job savedJob = buildJob(101L, 88L);
        JobResponseDto response = buildResponse(101L, 88L);

        when(modelMapper.map(request, Job.class)).thenReturn(mappedJob);
        when(jobRepository.save(mappedJob)).thenReturn(savedJob);
        when(modelMapper.map(savedJob, JobResponseDto.class)).thenReturn(response);

        JobResponseDto result = jobService.createJob(request, 88L, "RECRUITER");

        assertEquals(101L, result.getId());
        assertEquals(88L, mappedJob.getRecruiterId());
        assertNotNull(mappedJob.getCreatedAt());
        ArgumentCaptor<JobCreatedDto> eventCaptor = ArgumentCaptor.forClass(JobCreatedDto.class);
        verify(eventPublisher).publishJobCreatedEvent(eventCaptor.capture());
        assertEquals(request.getTitle(), eventCaptor.getValue().getJobTitle());
        assertEquals(request.getRecruiterEmail(), eventCaptor.getValue().getRecruiterEmail());
    }

    @Test
    @DisplayName("createJob should allow recruiter role in case-insensitive form")
    void createJob_caseInsensitiveRole_success() {
        JobRequestDto request = buildRequest();
        Job mappedJob = new Job();
        Job savedJob = buildJob(102L, 77L);
        JobResponseDto response = buildResponse(102L, 77L);

        when(modelMapper.map(request, Job.class)).thenReturn(mappedJob);
        when(jobRepository.save(mappedJob)).thenReturn(savedJob);
        when(modelMapper.map(savedJob, JobResponseDto.class)).thenReturn(response);

        JobResponseDto result = jobService.createJob(request, 77L, "recruiter");

        assertEquals(102L, result.getId());
        verify(eventPublisher, times(1)).publishJobCreatedEvent(any(JobCreatedDto.class));
    }

    @Test
    @DisplayName("createJob should reject non-recruiter role")
    void createJob_unauthorizedRole_throwsUnauthorized() {
        JobRequestDto request = buildRequest();

        UnauthorizedException ex = assertThrows(
                UnauthorizedException.class,
                () -> jobService.createJob(request, 88L, "CANDIDATE")
        );

        assertEquals("Only recruiters can post jobs", ex.getMessage());
        verify(jobRepository, never()).save(any(Job.class));
        verify(eventPublisher, never()).publishJobCreatedEvent(any(JobCreatedDto.class));
    }

    @Test
    @DisplayName("createJob should propagate publish failure (edge case)")
    void createJob_publishFails_throwsException() {
        JobRequestDto request = buildRequest();
        Job mappedJob = new Job();
        Job savedJob = buildJob(103L, 88L);

        when(modelMapper.map(request, Job.class)).thenReturn(mappedJob);
        when(jobRepository.save(mappedJob)).thenReturn(savedJob);
        doThrow(new RuntimeException("Rabbit publish failed"))
                .when(eventPublisher).publishJobCreatedEvent(any(JobCreatedDto.class));

        RuntimeException ex = assertThrows(
                RuntimeException.class,
                () -> jobService.createJob(request, 88L, "RECRUITER")
        );

        assertEquals("Rabbit publish failed", ex.getMessage());
    }

    @Test
    @DisplayName("getAllJobs should return mapped page using descending sort")
    void getAllJobs_descSort_success() {
        Job job = buildJob(201L, 90L);
        JobResponseDto response = buildResponse(201L, 90L);
        Page<Job> page = new PageImpl<>(List.of(job));

        when(jobRepository.findAll(any(Pageable.class))).thenReturn(page);
        when(modelMapper.map(job, JobResponseDto.class)).thenReturn(response);

        Page<JobResponseDto> result = jobService.getAllJobs(0, 10, "createdAt", "desc");

        assertEquals(1, result.getTotalElements());
        assertEquals(201L, result.getContent().get(0).getId());
        ArgumentCaptor<Pageable> pageableCaptor = ArgumentCaptor.forClass(Pageable.class);
        verify(jobRepository).findAll(pageableCaptor.capture());
        Sort.Order order = pageableCaptor.getValue().getSort().getOrderFor("createdAt");
        assertNotNull(order);
        assertEquals(Sort.Direction.DESC, order.getDirection());
    }

    @Test
    @DisplayName("getJobById should return mapped response when job exists")
    void getJobById_success() {
        Job job = buildJob(301L, 91L);
        JobResponseDto response = buildResponse(301L, 91L);

        when(jobRepository.findById(301L)).thenReturn(Optional.of(job));
        when(modelMapper.map(job, JobResponseDto.class)).thenReturn(response);

        JobResponseDto result = jobService.getJobById(301L);

        assertEquals(301L, result.getId());
    }

    @Test
    @DisplayName("getJobById should throw when job does not exist")
    void getJobById_notFound_throwsJobNotFound() {
        when(jobRepository.findById(404L)).thenReturn(Optional.empty());

        JobNotFoundException ex = assertThrows(
                JobNotFoundException.class,
                () -> jobService.getJobById(404L)
        );

        assertEquals("Job not found", ex.getMessage());
    }

    @Test
    @DisplayName("updateJob should update and save when recruiter owns the job")
    void updateJob_success() {
        JobRequestDto request = buildRequest();
        Job job = buildJob(401L, 92L);
        Job updatedJob = buildJob(401L, 92L);
        JobResponseDto response = buildResponse(401L, 92L);

        when(jobRepository.findById(401L)).thenReturn(Optional.of(job));
        doAnswer(invocation -> null).when(modelMapper).map(request, job);
        when(jobRepository.save(job)).thenReturn(updatedJob);
        when(modelMapper.map(updatedJob, JobResponseDto.class)).thenReturn(response);

        JobResponseDto result = jobService.updateJob(401L, request, 92L);

        assertEquals(401L, result.getId());
        verify(jobRepository).save(job);
    }

    @Test
    @DisplayName("updateJob should throw when job does not exist")
    void updateJob_notFound_throwsJobNotFound() {
        when(jobRepository.findById(405L)).thenReturn(Optional.empty());

        assertThrows(JobNotFoundException.class, () -> jobService.updateJob(405L, buildRequest(), 92L));
        verify(jobRepository, never()).save(any(Job.class));
    }

    @Test
    @DisplayName("updateJob should throw when recruiter does not own the job")
    void updateJob_unauthorized_throwsUnauthorized() {
        Job job = buildJob(406L, 999L);
        when(jobRepository.findById(406L)).thenReturn(Optional.of(job));

        UnauthorizedException ex = assertThrows(
                UnauthorizedException.class,
                () -> jobService.updateJob(406L, buildRequest(), 92L)
        );

        assertEquals("You are not allowed to update this job", ex.getMessage());
        verify(jobRepository, never()).save(any(Job.class));
    }

    @Test
    @DisplayName("deleteJob should delete and publish delete event when authorized")
    void deleteJob_success() {
        Job job = buildJob(501L, 93L);
        when(jobRepository.findById(501L)).thenReturn(Optional.of(job));

        jobService.deleteJob(501L, 93L);

        verify(jobRepository).delete(job);
        verify(eventPublisher).publishJobDeletedEvent(argThat((JobDeletedEvent deletedEvent) ->
                deletedEvent != null && deletedEvent.getJobId().equals(501L)));
    }

    @Test
    @DisplayName("deleteJob should throw when job does not exist")
    void deleteJob_notFound_throwsJobNotFound() {
        when(jobRepository.findById(502L)).thenReturn(Optional.empty());

        assertThrows(JobNotFoundException.class, () -> jobService.deleteJob(502L, 93L));
        verify(jobRepository, never()).delete(any(Job.class));
        verify(eventPublisher, never()).publishJobDeletedEvent(any());
    }

    @Test
    @DisplayName("deleteJob should throw when recruiter is not the owner")
    void deleteJob_unauthorized_throwsUnauthorized() {
        Job job = buildJob(503L, 1001L);
        when(jobRepository.findById(503L)).thenReturn(Optional.of(job));

        UnauthorizedException ex = assertThrows(
                UnauthorizedException.class,
                () -> jobService.deleteJob(503L, 93L)
        );

        assertEquals("You are not allowed to delete this job", ex.getMessage());
        verify(jobRepository, never()).delete(any(Job.class));
        verify(eventPublisher, never()).publishJobDeletedEvent(any());
    }

    @Test
    @DisplayName("deleteJob should propagate publish failure when no active transaction (edge case)")
    void deleteJob_publishFailsWithoutTransaction_throwsException() {
        Job job = buildJob(504L, 93L);
        when(jobRepository.findById(504L)).thenReturn(Optional.of(job));
        doThrow(new RuntimeException("Rabbit publish failed"))
                .when(eventPublisher).publishJobDeletedEvent(any());

        RuntimeException ex = assertThrows(
                RuntimeException.class,
                () -> jobService.deleteJob(504L, 93L)
        );

        assertEquals("Rabbit publish failed", ex.getMessage());
        verify(jobRepository).delete(job);
    }

    @Test
    @DisplayName("deleteJob should defer publish until afterCommit when transaction is active (edge case)")
    void deleteJob_withActiveTransaction_publishesAfterCommit() {
        Job job = buildJob(505L, 93L);
        when(jobRepository.findById(505L)).thenReturn(Optional.of(job));

        TransactionSynchronizationManager.initSynchronization();
        TransactionSynchronizationManager.setActualTransactionActive(true);
        try {
            jobService.deleteJob(505L, 93L);

            verify(jobRepository).delete(job);
            verify(eventPublisher, never()).publishJobDeletedEvent(any());

            for (TransactionSynchronization synchronization : TransactionSynchronizationManager.getSynchronizations()) {
                synchronization.afterCommit();
            }

            verify(eventPublisher).publishJobDeletedEvent(argThat((JobDeletedEvent deletedEvent) ->
                    deletedEvent != null && deletedEvent.getJobId().equals(505L)));
        } finally {
            TransactionSynchronizationManager.setActualTransactionActive(false);
            TransactionSynchronizationManager.clearSynchronization();
        }
    }

    @Test
    @DisplayName("searchJobs should return mapped page with ascending sort")
    void searchJobs_success() {
        JobFilterDto filter = new JobFilterDto();
        filter.setLocation("Pune");

        Job job = buildJob(601L, 94L);
        JobResponseDto response = buildResponse(601L, 94L);

        when(jobRepository.findAll(any(Specification.class), any(Pageable.class)))
                .thenReturn(new PageImpl<>(List.of(job)));
        when(modelMapper.map(job, JobResponseDto.class)).thenReturn(response);

        Page<JobResponseDto> result = jobService.searchJobs(filter, 0, 5, "salary", "asc");

        assertEquals(1, result.getTotalElements());
        assertEquals(601L, result.getContent().get(0).getId());

        ArgumentCaptor<Pageable> pageableCaptor = ArgumentCaptor.forClass(Pageable.class);
        verify(jobRepository).findAll(any(Specification.class), pageableCaptor.capture());
        Sort.Order order = pageableCaptor.getValue().getSort().getOrderFor("salary");
        assertNotNull(order);
        assertEquals(Sort.Direction.ASC, order.getDirection());
        assertEquals(PageRequest.of(0, 5).getPageNumber(), pageableCaptor.getValue().getPageNumber());
        assertEquals(PageRequest.of(0, 5).getPageSize(), pageableCaptor.getValue().getPageSize());
    }

    @Test
    @DisplayName("getRecruiterJobIds should return only recruiter owned ids for recruiter role")
    void getRecruiterJobIds_success() {
        when(jobRepository.findJobIdsByRecruiterId(100L)).thenReturn(List.of(11L, 12L, 13L));

        List<Long> result = jobService.getRecruiterJobIds(100L, 100L, "RECRUITER");

        assertEquals(List.of(11L, 12L, 13L), result);
    }

    @Test
    @DisplayName("getRecruiterJobIds should allow internal service role")
    void getRecruiterJobIds_internalService_success() {
        when(jobRepository.findJobIdsByRecruiterId(100L)).thenReturn(List.of(11L, 12L));

        List<Long> result = jobService.getRecruiterJobIds(100L, null, "INTERNAL_SERVICE");

        assertEquals(List.of(11L, 12L), result);
    }

    @Test
    @DisplayName("getRecruiterJobIds should throw for non recruiter and non internal role")
    void getRecruiterJobIds_forbidden() {
        UnauthorizedException ex = assertThrows(
                UnauthorizedException.class,
                () -> jobService.getRecruiterJobIds(100L, 100L, "CANDIDATE")
        );

        assertEquals("Only recruiters or internal service can access this endpoint", ex.getMessage());
        verify(jobRepository, never()).findJobIdsByRecruiterId(any());
    }

    @Test
    @DisplayName("getRecruiterJobIds should throw when recruiter requests other recruiter data")
    void getRecruiterJobIds_recruiterMismatch_forbidden() {
        UnauthorizedException ex = assertThrows(
                UnauthorizedException.class,
                () -> jobService.getRecruiterJobIds(100L, 101L, "RECRUITER")
        );

        assertEquals("Recruiters can only access their own recruiter dashboard data", ex.getMessage());
        verify(jobRepository, never()).findJobIdsByRecruiterId(any());
    }

    @Test
    @DisplayName("getRecruiterOpenRolesCount should return active jobs count")
    void getRecruiterOpenRolesCount_success() {
        when(jobRepository.countByRecruiterIdAndStatus(101L, JobStatus.ACTIVE)).thenReturn(7L);

        RecruiterOpenRolesCountDto result = jobService.getRecruiterOpenRolesCount(101L, 101L, "RECRUITER");

        assertEquals(7L, result.getOpenRoles());
    }

    @Test
    @DisplayName("getRecruiterRecentJobs should enforce max limit and sort by createdAt desc")
    void getRecruiterRecentJobs_success() {
        Job job1 = buildJob(701L, 88L);
        job1.setStatus(JobStatus.ACTIVE);
        Job job2 = buildJob(702L, 88L);
        job2.setStatus(JobStatus.CLOSED);

        when(jobRepository.findByRecruiterId(any(Long.class), any(Pageable.class))).thenReturn(List.of(job1, job2));

        List<RecruiterRecentJobDto> result = jobService.getRecruiterRecentJobs(88L, 20, 88L, "RECRUITER");

        assertEquals(2, result.size());
        assertEquals(701L, result.get(0).getJobId());
        assertEquals("ACTIVE", result.get(0).getStatus());
        assertEquals("CLOSED", result.get(1).getStatus());
        assertNotNull(result.get(0).getCreatedAt());

        ArgumentCaptor<Pageable> pageableCaptor = ArgumentCaptor.forClass(Pageable.class);
        verify(jobRepository).findByRecruiterId(eq(88L), pageableCaptor.capture());
        assertEquals(10, pageableCaptor.getValue().getPageSize());
        Sort.Order order = pageableCaptor.getValue().getSort().getOrderFor("createdAt");
        assertNotNull(order);
        assertTrue(order.isDescending());
    }

    @Test
    @DisplayName("getRecruiterRecentJobs should default to 5 when invalid limit is passed")
    void getRecruiterRecentJobs_defaultLimit() {
        when(jobRepository.findByRecruiterId(any(Long.class), any(Pageable.class))).thenReturn(List.of());

        jobService.getRecruiterRecentJobs(90L, 0, 90L, "RECRUITER");

        ArgumentCaptor<Pageable> pageableCaptor = ArgumentCaptor.forClass(Pageable.class);
        verify(jobRepository).findByRecruiterId(eq(90L), pageableCaptor.capture());
        assertEquals(5, pageableCaptor.getValue().getPageSize());
    }

    @Test
    @DisplayName("getRecruiterJobSummary should return total and status counts")
    void getRecruiterJobSummary_success() {
        when(jobRepository.countByRecruiterId(10L)).thenReturn(20L);
        when(jobRepository.countByRecruiterIdAndStatus(10L, JobStatus.ACTIVE)).thenReturn(11L);
        when(jobRepository.countByRecruiterIdAndStatus(10L, JobStatus.DRAFT)).thenReturn(6L);
        when(jobRepository.countByRecruiterIdAndStatus(10L, JobStatus.CLOSED)).thenReturn(3L);

        RecruiterJobSummaryDto result = jobService.getRecruiterJobSummary(10L, null, "INTERNAL_SERVICE");

        assertEquals(20L, result.getTotalJobs());
        assertEquals(11L, result.getActiveJobs());
        assertEquals(6L, result.getDraftJobs());
        assertEquals(3L, result.getClosedJobs());
    }

    @Test
    @DisplayName("getJobsByRecruiterId should return mapped recruiter jobs")
    void getJobsByRecruiterId_success() {
        Job job1 = buildJob(801L, 42L);
        Job job2 = buildJob(802L, 42L);

        when(jobRepository.findAllByRecruiterId(42L)).thenReturn(List.of(job1, job2));
        when(modelMapper.map(job1, JobResponseDto.class)).thenReturn(buildResponse(801L, 42L));
        when(modelMapper.map(job2, JobResponseDto.class)).thenReturn(buildResponse(802L, 42L));

        List<JobResponseDto> result = jobService.getJobsByRecruiterId(42L);

        assertEquals(2, result.size());
        assertEquals(801L, result.get(0).getId());
        assertEquals(802L, result.get(1).getId());
    }

    private JobRequestDto buildRequest() {
        JobRequestDto dto = new JobRequestDto();
        dto.setTitle("Java Developer");
        dto.setCompanyName("Acme");
        dto.setLocation("Pune");
        dto.setSalary(12.5);
        dto.setExperience(3);
        dto.setDescription("Backend role");
        dto.setRecruiterEmail("recruiter@acme.com");
        dto.setJobType("FULL_TIME");
        return dto;
    }

    private Job buildJob(Long id, Long recruiterId) {
        Job job = new Job();
        job.setId(id);
        job.setTitle("Java Developer");
        job.setCompanyName("Acme");
        job.setLocation("Pune");
        job.setSalary(12.5);
        job.setExperience(3);
        job.setDescription("Backend role");
        job.setJobType(com.capg.JobService.entity.JobType.FULL_TIME);
        job.setStatus(JobStatus.ACTIVE);
        job.setRecruiterId(recruiterId);
        job.setCreatedAt(LocalDateTime.now());
        return job;
    }

    private JobResponseDto buildResponse(Long id, Long recruiterId) {
        JobResponseDto dto = new JobResponseDto();
        dto.setId(id);
        dto.setTitle("Java Developer");
        dto.setCompanyName("Acme");
        dto.setLocation("Pune");
        dto.setSalary(12.5);
        dto.setExperience(3);
        dto.setDescription("Backend role");
        dto.setJobType("FULL_TIME");
        dto.setRecruiterId(recruiterId);
        dto.setCreatedAt(LocalDateTime.now());
        return dto;
    }
}
