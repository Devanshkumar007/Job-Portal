package com.capg.NotificationService.consumer;

import com.capg.NotificationService.dto.ApplicationCreatedDto;
import com.capg.NotificationService.dto.ApplicationStatusDto;
import com.capg.NotificationService.dto.JobCreatedDto;
import com.capg.NotificationService.service.EmailService;
import lombok.RequiredArgsConstructor;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class NotificationConsumer {

    private final EmailService emailService;

    @RabbitListener(queues = "notification.application.created.queue")
    public void handleApplicationCreated(ApplicationCreatedDto event) {

        emailService.sendEmail(
                event.getApplicantEmail(),
                "Application Received",
                "You applied for " + event.getJobTitle() + " at " + event.getCompany()
        );
    }

    @RabbitListener(queues = "notification.application.status.queue")
    public void handleApplicationStatus(ApplicationStatusDto event) {

        emailService.sendEmail(
                event.getUserEmail(),
                "Application Status Update",
                "Your application for " + event.getJobTitle() + " at " + event.getCompany()
                        + " is " + event.getStatus()
        );
    }

    @RabbitListener(queues = "notification.job.created.queue")
    public void handleJobCreated(JobCreatedDto event) {

        emailService.sendEmail(
                event.getRecruiterEmail(),
                "Job Posted Successfully",
                "Your job " + event.getJobTitle() + " is live now"
        );
    }
}