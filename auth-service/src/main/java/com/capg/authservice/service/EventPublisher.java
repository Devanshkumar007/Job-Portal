package com.capg.authservice.service;

import com.capg.authservice.dto.response.UserDeletedEvent;
import lombok.RequiredArgsConstructor;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class EventPublisher {

    private final RabbitTemplate rabbitTemplate;

    public void publishUserDeletedEvent(UserDeletedEvent event) {

        rabbitTemplate.convertAndSend(
                "job-portal-exchange",
                "job.user.deleted",
                event
        );
    }
}