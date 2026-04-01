package com.capg.JobService.service;

import com.capg.JobService.dto.JobDeletedEvent;
import com.capg.JobService.dto.UserDeletedEvent;
import com.capg.JobService.entity.Job;
import com.capg.JobService.repository.JobRepository;
import com.capg.JobService.config.RabbitMQConfig;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.stereotype.Component;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import java.util.List;

@Component
@RequiredArgsConstructor
@Slf4j
public class UserDeletionConsumer {

    private static final String JOB_BY_ID_CACHE = "jobById";
    private static final String JOBS_PAGE_CACHE = "jobsPage";
    private static final String JOBS_SEARCH_CACHE = "jobsSearch";

    private final JobRepository jobRepository;
    private final EventPublisher eventPublisher;
    private final CacheManager cacheManager;

    @RabbitListener(queues = RabbitMQConfig.USER_DELETED_QUEUE)
    @Transactional
    public void handleUserDeleted(UserDeletedEvent event) {

        if (!"RECRUITER".equals(event.getRole())) {
            return;
        }

        List<Job> jobs = jobRepository.findAllByRecruiterId(event.getUserId());
        if (jobs.isEmpty()) {
            return;
        }

        List<Long> deletedJobIds = jobs.stream().map(Job::getId).toList();
        jobRepository.deleteAll(jobs);
        evictJobCaches();
        publishJobDeletedEventsAfterCommit(deletedJobIds);
        log.info("Deleted {} jobs for recruiterId={}", deletedJobIds.size(), event.getUserId());
    }

    private void publishJobDeletedEventsAfterCommit(List<Long> jobIds) {
        if (!TransactionSynchronizationManager.isActualTransactionActive()) {
            jobIds.forEach(this::publishJobDeletedEvent);
            return;
        }

        TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
            @Override
            public void afterCommit() {
                jobIds.forEach(id -> {
                    try {
                        publishJobDeletedEvent(id);
                    } catch (Exception ex) {
                        log.error("Job deleted in DB but failed to publish deletion event for jobId={}", id, ex);
                    }
                });
            }
        });
    }

    private void publishJobDeletedEvent(Long jobId) {
        eventPublisher.publishJobDeletedEvent(new JobDeletedEvent(jobId));
    }

    private void evictJobCaches() {
        clearCache(JOB_BY_ID_CACHE);
        clearCache(JOBS_PAGE_CACHE);
        clearCache(JOBS_SEARCH_CACHE);
    }

    private void clearCache(String cacheName) {
        Cache cache = cacheManager.getCache(cacheName);
        if (cache != null) {
            cache.clear();
        }
    }
}
