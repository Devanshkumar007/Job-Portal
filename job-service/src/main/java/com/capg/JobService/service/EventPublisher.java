package com.capg.JobService.service;

import com.capg.JobService.dto.JobCreatedDto;
import com.capg.JobService.dto.JobDeletedEvent;
import com.capg.JobService.config.RabbitMQConfig;
import lombok.RequiredArgsConstructor;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class EventPublisher {

    private final RabbitTemplate rabbitTemplate;

    public void publishJobCreatedEvent(
            JobCreatedDto event) {

        rabbitTemplate.convertAndSend(
                RabbitMQConfig.EXCHANGE,
                RabbitMQConfig.JOB_CREATED_ROUTING_KEY,
                event
        );
    }

    public void publishJobDeletedEvent(JobDeletedEvent event){
        rabbitTemplate.convertAndSend(
                RabbitMQConfig.EXCHANGE,
                RabbitMQConfig.JOB_DELETED_ROUTING_KEY,
                new JobDeletedEvent(event.getJobId())
        );
    }
}
