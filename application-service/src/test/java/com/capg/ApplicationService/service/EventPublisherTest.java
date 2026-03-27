package com.capg.ApplicationService.service;

import com.capg.ApplicationService.dto.ApplicationCreatedDto;
import com.capg.ApplicationService.dto.ApplicationStatusDto;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.amqp.rabbit.core.RabbitTemplate;

import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class EventPublisherTest {

    @Mock
    private RabbitTemplate rabbitTemplate;

    @InjectMocks
    private EventPublisher eventPublisher;

    @Test
    @Order(1)
    void publishApplicationCreatedEvent_sends_to_expected_routing_key() {
        ApplicationCreatedDto event =
                new ApplicationCreatedDto("a@x.com", "Backend Engineer", "Acme", "APPLIED");

        eventPublisher.publishApplicationCreatedEvent(event);

        verify(rabbitTemplate).convertAndSend(
                "job-portal-exchange",
                "notification.application.created",
                event
        );
    }

    @Test
    @Order(2)
    void publishApplicationStatusEvent_sends_to_expected_routing_key() {
        ApplicationStatusDto event =
                new ApplicationStatusDto("a@x.com", "Backend Engineer", "SHORTLISTED", "Acme");

        eventPublisher.publishApplicationStausEvent(event);

        verify(rabbitTemplate).convertAndSend(
                "job-portal-exchange",
                "notification.application.status.changed",
                event
        );
    }
}
