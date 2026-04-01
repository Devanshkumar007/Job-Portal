package com.capg.JobService.service;

import com.capg.JobService.dto.JobDeletedEvent;
import com.capg.JobService.dto.UserDeletedEvent;
import com.capg.JobService.entity.Job;
import com.capg.JobService.repository.JobRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentMatcher;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;

import java.util.List;

import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UserDeletionConsumerTest {

    @Mock
    private JobRepository jobRepository;

    @Mock
    private EventPublisher eventPublisher;

    @Mock
    private CacheManager cacheManager;

    @Mock
    private Cache jobByIdCache;

    @Mock
    private Cache jobsPageCache;

    @Mock
    private Cache jobsSearchCache;

    @InjectMocks
    private UserDeletionConsumer userDeletionConsumer;

    @Test
    @DisplayName("handleUserDeleted should delete jobs and publish delete events for recruiter user")
    void handleUserDeleted_recruiterRole_success() {
        UserDeletedEvent event = new UserDeletedEvent(501L, "RECRUITER");

        Job first = new Job();
        first.setId(1001L);
        first.setRecruiterId(501L);

        Job second = new Job();
        second.setId(1002L);
        second.setRecruiterId(501L);

        when(cacheManager.getCache("jobById")).thenReturn(jobByIdCache);
        when(cacheManager.getCache("jobsPage")).thenReturn(jobsPageCache);
        when(cacheManager.getCache("jobsSearch")).thenReturn(jobsSearchCache);
        when(jobRepository.findAllByRecruiterId(501L)).thenReturn(List.of(first, second));

        userDeletionConsumer.handleUserDeleted(event);

        verify(jobRepository).findAllByRecruiterId(501L);
        verify(jobRepository).deleteAll(List.of(first, second));
        verify(jobByIdCache).clear();
        verify(jobsPageCache).clear();
        verify(jobsSearchCache).clear();
        verify(eventPublisher).publishJobDeletedEvent(argThat(jobDeletedEventWithId(1001L)));
        verify(eventPublisher).publishJobDeletedEvent(argThat(jobDeletedEventWithId(1002L)));
    }

    @Test
    @DisplayName("handleUserDeleted should not delete jobs for non-recruiter role")
    void handleUserDeleted_nonRecruiterRole_noDelete() {
        UserDeletedEvent event = new UserDeletedEvent(502L, "CANDIDATE");

        userDeletionConsumer.handleUserDeleted(event);

        verify(jobRepository, never()).findAllByRecruiterId(502L);
        verify(jobRepository, never()).deleteAll(org.mockito.ArgumentMatchers.<List<Job>>any());
        verify(cacheManager, never()).getCache(org.mockito.ArgumentMatchers.anyString());
        verify(eventPublisher, never()).publishJobDeletedEvent(org.mockito.ArgumentMatchers.any(JobDeletedEvent.class));
    }

    @Test
    @DisplayName("handleUserDeleted should not delete jobs when role is null (edge case)")
    void handleUserDeleted_nullRole_noDelete() {
        UserDeletedEvent event = new UserDeletedEvent(503L, null);

        userDeletionConsumer.handleUserDeleted(event);

        verify(jobRepository, never()).findAllByRecruiterId(503L);
        verify(jobRepository, never()).deleteAll(org.mockito.ArgumentMatchers.<List<Job>>any());
        verify(cacheManager, never()).getCache(org.mockito.ArgumentMatchers.anyString());
        verify(eventPublisher, never()).publishJobDeletedEvent(org.mockito.ArgumentMatchers.any(JobDeletedEvent.class));
    }

    @Test
    @DisplayName("handleUserDeleted should not publish events when recruiter has no jobs")
    void handleUserDeleted_recruiterRole_noJobs() {
        UserDeletedEvent event = new UserDeletedEvent(504L, "RECRUITER");
        when(jobRepository.findAllByRecruiterId(504L)).thenReturn(List.of());

        userDeletionConsumer.handleUserDeleted(event);

        verify(jobRepository).findAllByRecruiterId(504L);
        verify(jobRepository, never()).deleteAll(org.mockito.ArgumentMatchers.<List<Job>>any());
        verify(cacheManager, never()).getCache(org.mockito.ArgumentMatchers.anyString());
        verify(eventPublisher, never()).publishJobDeletedEvent(org.mockito.ArgumentMatchers.any(JobDeletedEvent.class));
    }

    private ArgumentMatcher<JobDeletedEvent> jobDeletedEventWithId(Long expectedId) {
        return event -> event != null && expectedId.equals(event.getJobId());
    }
}
