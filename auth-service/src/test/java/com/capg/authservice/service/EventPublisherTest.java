package com.capg.authservice.service;

import com.capg.authservice.dto.response.UserDeletedEvent;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.amqp.rabbit.core.RabbitTemplate;

import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class EventPublisherTest {

    @Mock
    private RabbitTemplate rabbitTemplate;

    @Test
    void publishUserDeletedEventShouldUseExpectedExchangeAndRoutingKey() {
        EventPublisher publisher = new EventPublisher(rabbitTemplate);
        UserDeletedEvent event = new UserDeletedEvent(42L, "JOB_SEEKER");

        publisher.publishUserDeletedEvent(event);

        verify(rabbitTemplate).convertAndSend("job-portal-exchange", "user.deleted", event);
    }
}
