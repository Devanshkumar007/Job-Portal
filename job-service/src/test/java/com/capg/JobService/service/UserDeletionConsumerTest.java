package com.capg.JobService.service;

import com.capg.JobService.dto.UserDeletedEvent;
import com.capg.JobService.repository.JobRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class UserDeletionConsumerTest {

    @Mock
    private JobRepository jobRepository;

    @InjectMocks
    private UserDeletionConsumer userDeletionConsumer;

    @Test
    @DisplayName("handleUserDeleted should delete jobs for recruiter user")
    void handleUserDeleted_recruiterRole_success() {
        UserDeletedEvent event = new UserDeletedEvent(501L, "RECRUITER");

        userDeletionConsumer.handleUserDeleted(event);

        verify(jobRepository).deleteByRecruiterId(501L);
    }

    @Test
    @DisplayName("handleUserDeleted should not delete jobs for non-recruiter role")
    void handleUserDeleted_nonRecruiterRole_noDelete() {
        UserDeletedEvent event = new UserDeletedEvent(502L, "CANDIDATE");

        userDeletionConsumer.handleUserDeleted(event);

        verify(jobRepository, never()).deleteByRecruiterId(502L);
    }

    @Test
    @DisplayName("handleUserDeleted should not delete jobs when role is null (edge case)")
    void handleUserDeleted_nullRole_noDelete() {
        UserDeletedEvent event = new UserDeletedEvent(503L, null);

        userDeletionConsumer.handleUserDeleted(event);

        verify(jobRepository, never()).deleteByRecruiterId(503L);
    }
}
