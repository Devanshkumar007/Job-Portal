package com.capg.ApplicationService.service;

import com.capg.ApplicationService.dto.ApplicationCreatedDto;
import com.capg.ApplicationService.dto.ApplicationStatusDto;
import com.capg.ApplicationService.dto.StatusUpdateRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class EventPublisher {

    private final RabbitTemplate rabbitTemplate;

    public void publishApplicationCreatedEvent(
            ApplicationCreatedDto event) {

        rabbitTemplate.convertAndSend(
                "job-portal-exchange",
                "notification.application.created",
                event
        );
    }
    public void publishApplicationStausEvent(
            ApplicationStatusDto event) {

        rabbitTemplate.convertAndSend(
                "job-portal-exchange",
                "notification.application.status.changed",
                event
        );
    }
}
