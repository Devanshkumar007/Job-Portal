package com.capg.JobService.service;

import com.capg.JobService.dto.*;
import com.capg.JobService.entity.Job;
import com.capg.JobService.entity.JobStatus;
import com.capg.JobService.entity.JobType;
import com.capg.JobService.exceptions.JobNotFoundException;
import com.capg.JobService.exceptions.UnauthorizedException;
import com.capg.JobService.repository.JobRepository;
import com.capg.JobService.specification.JobSpecification;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.CachePut;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;
import java.time.LocalDateTime;
import java.util.List;


@Service
@Slf4j
@RequiredArgsConstructor
public class JobServiceImpl implements JobService {

    private final JobRepository jobRepository;
    private final ModelMapper modelMapper;
    private final EventPublisher eventPublisher;


    @Override
    @CacheEvict(value = {"jobsPage", "jobsSearch"}, allEntries = true)
    @CachePut(value = "jobById", key = "#result.id", unless = "#result == null")
    public JobResponseDto createJob(JobRequestDto dto, Long recruiterId, String role) {
        System.out.println("[CACHE-DEBUG] createJob executed -> evict jobsPage/jobsSearch and put jobById");

        if (!"RECRUITER".equalsIgnoreCase(role)) {
            throw new UnauthorizedException("Only recruiters can post jobs");
        }

        Job job = modelMapper.map(dto, Job.class);
        applyJobTypeFields(job, dto);

        job.setRecruiterId(recruiterId);
        job.setCreatedAt(LocalDateTime.now());

        Job saved = jobRepository.save(job);


        JobCreatedDto event =
                new JobCreatedDto(
                        dto.getTitle(),
                        dto.getRecruiterEmail()
                );

        eventPublisher.publishJobCreatedEvent(event);

        return toResponse(saved);
    }


    @Override
    @Cacheable(value = "jobsPage", key = "#page + ':' + #size + ':' + #sortBy + ':' + #direction")
    public Page<JobResponseDto> getAllJobs(int page, int size, String sortBy, String direction) {
        System.out.println("[CACHE-DEBUG] getAllJobs DB call -> key=" + page + ":" + size + ":" + sortBy + ":" + direction);

        Sort sort = direction.equalsIgnoreCase("desc") ?
                Sort.by(sortBy).descending() :
                Sort.by(sortBy).ascending();

        Pageable pageable = PageRequest.of(page, size, sort);

        Page<Job> jobPage = jobRepository.findAll(pageable);

        return jobPage.map(this::toResponse);
    }


    @Override
    @Cacheable(value = "jobById", key = "#id", unless = "#result == null")
    public JobResponseDto getJobById(Long id) {
        System.out.println("[CACHE-DEBUG] getJobById DB call -> id=" + id);
        Job job = jobRepository.findById(id)
                .orElseThrow(() -> new JobNotFoundException("Job not found"));

        return toResponse(job);
    }


    @Override
    @CachePut(value = "jobById", key = "#id", unless = "#result == null")
    @CacheEvict(value = {"jobsPage", "jobsSearch"}, allEntries = true)
    public JobResponseDto updateJob(Long id, JobRequestDto dto, Long recruiterId) {
        System.out.println("[CACHE-DEBUG] updateJob executed -> put jobById and evict jobsPage/jobsSearch, id=" + id);

        Job job = jobRepository.findById(id)
                .orElseThrow(() -> new JobNotFoundException("Job not found"));

        if (!job.getRecruiterId().equals(recruiterId)) {
            throw new UnauthorizedException("You are not allowed to update this job");
        }

        modelMapper.map(dto, job); // update fields
        applyJobTypeFields(job, dto);

        Job updated = jobRepository.save(job);

        return toResponse(updated);
    }


    @Override
    @Transactional
    @CacheEvict(value = {"jobById", "jobsPage", "jobsSearch"}, allEntries = true)
    public void deleteJob(Long id, Long recruiterId) {
        System.out.println("[CACHE-DEBUG] deleteJob executed -> evict jobById/jobsPage/jobsSearch, id=" + id);

        Job job = jobRepository.findById(id)
                .orElseThrow(() -> new JobNotFoundException("Job not found"));

        if (!job.getRecruiterId().equals(recruiterId)) {
            throw new UnauthorizedException("You are not allowed to delete this job");
        }

        jobRepository.delete(job);
        JobDeletedEvent event = new JobDeletedEvent(job.getId());
        publishJobDeletedEventAfterCommit(event);
    }

    private void publishJobDeletedEventAfterCommit(JobDeletedEvent event) {
        if (!TransactionSynchronizationManager.isActualTransactionActive()) {
            eventPublisher.publishJobDeletedEvent(event);
            return;
        }

        TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
            @Override
            public void afterCommit() {
                try {
                    eventPublisher.publishJobDeletedEvent(event);
                } catch (Exception ex) {
                    log.error("Job deleted in DB but failed to publish deletion event for jobId={}", event.getJobId(), ex);
                }
            }
        });
    }


    @Override
    @Cacheable(
            value = "jobsSearch",
            key = "T(java.util.Objects).hash(#filter) + ':' + #page + ':' + #size + ':' + #sortBy + ':' + #direction"
    )
    public Page<JobResponseDto> searchJobs(
            JobFilterDto filter,
            int page,
            int size,
            String sortBy,
            String direction) {
        System.out.println("[CACHE-DEBUG] searchJobs DB call -> key=" + java.util.Objects.hash(filter) + ":" + page + ":" + size + ":" + sortBy + ":" + direction);

        Sort sort = direction.equalsIgnoreCase("desc") ?
                Sort.by(sortBy).descending() :
                Sort.by(sortBy).ascending();

        Pageable pageable = PageRequest.of(page, size, sort);

        Page<Job> jobPage = jobRepository.findAll(
                JobSpecification.getFilteredJobs(filter),
                pageable
        );

        return jobPage.map(this::toResponse);
    }

    @Override
    public List<Long> getRecruiterJobIds(Long recruiterId, Long requesterId, String role) {
        validateInternalRecruiterAccess(recruiterId, requesterId, role);
        return jobRepository.findJobIdsByRecruiterId(recruiterId);
    }

    @Override
    public RecruiterOpenRolesCountDto getRecruiterOpenRolesCount(Long recruiterId, Long requesterId, String role) {
        validateInternalRecruiterAccess(recruiterId, requesterId, role);
        long openRoles = jobRepository.countByRecruiterIdAndStatus(recruiterId, JobStatus.ACTIVE);
        return new RecruiterOpenRolesCountDto(openRoles);
    }

    @Override
    public List<RecruiterRecentJobDto> getRecruiterRecentJobs(Long recruiterId, int limit, Long requesterId, String role) {
        validateInternalRecruiterAccess(recruiterId, requesterId, role);
        int sanitizedLimit = limit <= 0 ? 5 : Math.min(limit, 10);
        List<Job> jobs = jobRepository.findByRecruiterId(
                recruiterId,
                PageRequest.of(0, sanitizedLimit, Sort.by("createdAt").descending())
        );

        return jobs.stream()
                .map(job -> new RecruiterRecentJobDto(
                        job.getId(),
                        job.getTitle(),
                        job.getCompanyName(),
                        job.getStatus() == null ? null : job.getStatus().name(),
                        job.getCreatedAt()
                ))
                .toList();
    }

    @Override
    public RecruiterJobSummaryDto getRecruiterJobSummary(Long recruiterId, Long requesterId, String role) {
        validateInternalRecruiterAccess(recruiterId, requesterId, role);

        long totalJobs = jobRepository.countByRecruiterId(recruiterId);
        long activeJobs = jobRepository.countByRecruiterIdAndStatus(recruiterId, JobStatus.ACTIVE);
        long draftJobs = jobRepository.countByRecruiterIdAndStatus(recruiterId, JobStatus.DRAFT);
        long closedJobs = jobRepository.countByRecruiterIdAndStatus(recruiterId, JobStatus.CLOSED);

        return new RecruiterJobSummaryDto(totalJobs, activeJobs, draftJobs, closedJobs);
    }

    @Override
    public List<JobResponseDto> getJobsByRecruiterId(Long recruiterId) {
        return jobRepository.findAllByRecruiterId(recruiterId)
                .stream()
                .map(this::toResponse)
                .toList();
    }

    private void applyJobTypeFields(Job job, JobRequestDto dto) {
        JobType parsedJobType = parseJobType(dto.getJobType());

        if (parsedJobType == JobType.INTERNSHIP) {
            if (dto.getInternshipDurationMonths() == null) {
                throw new IllegalArgumentException("internshipDurationMonths is required when jobType is INTERNSHIP");
            }
            job.setInternshipDurationMonths(dto.getInternshipDurationMonths());
        } else {
            if (dto.getInternshipDurationMonths() != null) {
                throw new IllegalArgumentException("internshipDurationMonths must be null unless jobType is INTERNSHIP");
            }
            job.setInternshipDurationMonths(null);
        }

        job.setJobType(parsedJobType);
    }

    private JobType parseJobType(String jobType) {
        try {
            return JobType.valueOf(jobType.trim().toUpperCase());
        } catch (Exception ex) {
            throw new IllegalArgumentException("jobType must be one of: FULL_TIME, PART_TIME, INTERNSHIP");
        }
    }

    private JobResponseDto toResponse(Job job) {
        JobResponseDto response = modelMapper.map(job, JobResponseDto.class);
        response.setJobType(job.getJobType() == null ? null : job.getJobType().name());
        response.setInternshipDurationMonths(job.getInternshipDurationMonths());
        return response;
    }

    private void validateInternalRecruiterAccess(Long recruiterId, Long requesterId, String role) {
        if (role == null || role.isBlank()) {
            throw new UnauthorizedException("Missing role for internal recruiter endpoint");
        }

        if ("INTERNAL_SERVICE".equalsIgnoreCase(role)) {
            return;
        }

        if (!"RECRUITER".equalsIgnoreCase(role)) {
            throw new UnauthorizedException("Only recruiters or internal service can access this endpoint");
        }

        if (requesterId == null || !requesterId.equals(recruiterId)) {
            throw new UnauthorizedException("Recruiters can only access their own recruiter dashboard data");
        }
    }
}
