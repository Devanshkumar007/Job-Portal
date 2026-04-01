package com.capg.JobService.service;

import com.capg.JobService.config.RabbitMQConfig;
import com.capg.JobService.dto.JobCreatedDto;
import com.capg.JobService.dto.JobDeletedEvent;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.amqp.rabbit.core.RabbitTemplate;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class EventPublisherTest {

    @Mock
    private RabbitTemplate rabbitTemplate;

    @InjectMocks
    private EventPublisher eventPublisher;

    @Test
    @DisplayName("publishJobCreatedEvent should send to expected exchange and routing key")
    void publishJobCreatedEvent_success() {
        JobCreatedDto event = new JobCreatedDto("SDE-2", "recruiter@acme.com");

        eventPublisher.publishJobCreatedEvent(event);

        verify(rabbitTemplate).convertAndSend(
                eq(RabbitMQConfig.EXCHANGE),
                eq(RabbitMQConfig.JOB_CREATED_ROUTING_KEY),
                eq(event)
        );
    }

    @Test
    @DisplayName("publishJobDeletedEvent should send normalized payload")
    void publishJobDeletedEvent_success() {
        JobDeletedEvent event = new JobDeletedEvent(901L);

        eventPublisher.publishJobDeletedEvent(event);

        verify(rabbitTemplate).convertAndSend(
                eq(RabbitMQConfig.EXCHANGE),
                eq(RabbitMQConfig.JOB_DELETED_ROUTING_KEY),
                eq(new JobDeletedEvent(901L))
        );
    }

    @Test
    @DisplayName("publishJobDeletedEvent should propagate RabbitTemplate failure (edge case)")
    void publishJobDeletedEvent_rabbitFails_throwsException() {
        JobDeletedEvent event = new JobDeletedEvent(902L);
        RuntimeException failure = new RuntimeException("Broker unavailable");
        doThrow(failure).when(rabbitTemplate)
                .convertAndSend(eq(RabbitMQConfig.EXCHANGE), eq(RabbitMQConfig.JOB_DELETED_ROUTING_KEY), eq(new JobDeletedEvent(902L)));

        RuntimeException ex = assertThrows(
                RuntimeException.class,
                () -> eventPublisher.publishJobDeletedEvent(event)
        );

        assertEquals("Broker unavailable", ex.getMessage());
        verify(rabbitTemplate).convertAndSend(
                eq(RabbitMQConfig.EXCHANGE),
                eq(RabbitMQConfig.JOB_DELETED_ROUTING_KEY),
                eq(new JobDeletedEvent(902L))
        );
    }
}
