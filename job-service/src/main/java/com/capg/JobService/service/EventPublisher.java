package com.capg.JobService.service;

import com.capg.JobService.dto.JobCreatedDto;
import com.capg.JobService.dto.JobDeletedEvent;
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
                "job-portal-exchange",
                "notification.job.created",
                event
        );
    }

    public void publishJobDeletedEvent(JobDeletedEvent event){
        rabbitTemplate.convertAndSend(
                "job-portal-exchange",
                "job.deleted",
                new JobDeletedEvent(event.getJobId())
        );
    }
}
