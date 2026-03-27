package com.capg.ApplicationService.service;

import com.capg.ApplicationService.dto.JobDeletedEvent;
import com.capg.ApplicationService.repository.ApplicationRepository;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class JobDeletionConsumerTest {

    @Mock
    private ApplicationRepository repository;

    @InjectMocks
    private JobDeletionConsumer consumer;

    @Test
    @Order(1)
    void handleJobDeleted_deletes_applications_by_job_id() {
        JobDeletedEvent event = new JobDeletedEvent(22L);

        consumer.handleJobDeleted(event);

        verify(repository).deleteByJobId(22L);
    }

    @Test
    @Order(2)
    void handleJobDeleted_null_event_edge_case_throws_null_pointer() {
        assertThrows(NullPointerException.class, () -> consumer.handleJobDeleted(null));
    }
}
