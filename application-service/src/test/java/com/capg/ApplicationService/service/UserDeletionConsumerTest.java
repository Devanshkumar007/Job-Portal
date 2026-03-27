package com.capg.ApplicationService.service;

import com.capg.ApplicationService.dto.UserDeletedEvent;
import com.capg.ApplicationService.repository.ApplicationRepository;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class UserDeletionConsumerTest {

    @Mock
    private ApplicationRepository applicationRepository;

    @InjectMocks
    private UserDeletionConsumer consumer;

    @Test
    @Order(1)
    void handleUserDeleted_jobSeeker_deletes_user_applications() {
        UserDeletedEvent event = new UserDeletedEvent(11L, "JOB_SEEKER");

        consumer.handleUserDeleted(event);

        verify(applicationRepository).deleteByUserId(11L);
    }

    @Test
    @Order(2)
    void handleUserDeleted_non_job_seeker_does_not_delete() {
        UserDeletedEvent event = new UserDeletedEvent(11L, "RECRUITER");

        consumer.handleUserDeleted(event);

        verify(applicationRepository, never()).deleteByUserId(11L);
    }

    @Test
    @Order(3)
    void handleUserDeleted_null_role_edge_case_does_not_delete() {
        UserDeletedEvent event = new UserDeletedEvent(11L, null);

        consumer.handleUserDeleted(event);

        verify(applicationRepository, never()).deleteByUserId(11L);
    }
}
