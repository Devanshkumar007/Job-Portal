package com.capg.JobService.service;

import com.capg.JobService.dto.*;
import com.capg.JobService.entity.Job;
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

        job.setRecruiterId(recruiterId);
        job.setCreatedAt(LocalDateTime.now());

        Job saved = jobRepository.save(job);


        JobCreatedDto event =
                new JobCreatedDto(
                        dto.getTitle(),
                        dto.getRecruiterEmail()
                );

        eventPublisher.publishJobCreatedEvent(event);

        return modelMapper.map(saved, JobResponseDto.class);
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

        return jobPage.map(job -> modelMapper.map(job, JobResponseDto.class));
    }


    @Override
    @Cacheable(value = "jobById", key = "#id", unless = "#result == null")
    public JobResponseDto getJobById(Long id) {
        System.out.println("[CACHE-DEBUG] getJobById DB call -> id=" + id);
        Job job = jobRepository.findById(id)
                .orElseThrow(() -> new JobNotFoundException("Job not found"));

        return modelMapper.map(job, JobResponseDto.class);
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

        Job updated = jobRepository.save(job);

        return modelMapper.map(updated, JobResponseDto.class);
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

        return jobPage.map(job -> modelMapper.map(job, JobResponseDto.class));
    }
}
