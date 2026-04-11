package com.capg.NotificationService.consumer;

import com.capg.NotificationService.dto.ApplicationCreatedDto;
import com.capg.NotificationService.dto.ApplicationStatusDto;
import com.capg.NotificationService.dto.ForgotPasswordEvent;
import com.capg.NotificationService.dto.InterviewScheduledDto;
import com.capg.NotificationService.dto.JobCreatedDto;
import com.capg.NotificationService.dto.OfferSentDto;
import com.capg.NotificationService.service.EmailService;
import com.capg.NotificationService.service.EmailTemplateService;
import lombok.RequiredArgsConstructor;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class NotificationConsumer {

    private final EmailService emailService;
    private final EmailTemplateService templateService;

    @RabbitListener(queues = "notification.application.created.queue")
    public void handleApplicationCreated(ApplicationCreatedDto event) {
        String html = templateService.applicationCreated(
                event.getJobTitle(),
                event.getCompany(),
                event.getStatus()
        );
        emailService.sendEmail(
                event.getApplicantEmail(),
                "Application Received — " + event.getJobTitle() + " at " + event.getCompany(),
                html
        );
    }

    @RabbitListener(queues = "notification.application.status.queue")
    public void handleApplicationStatus(ApplicationStatusDto event) {
        String html = templateService.applicationStatusUpdate(
                event.getJobTitle(),
                event.getCompany(),
                event.getStatus()
        );
        emailService.sendEmail(
                event.getUserEmail(),
                "Application Update — " + event.getStatus() + " · " + event.getJobTitle(),
                html
        );
    }

    @RabbitListener(queues = "notification.job.created.queue")
    public void handleJobCreated(JobCreatedDto event) {
        String html = templateService.jobCreated(event.getJobTitle());
        emailService.sendEmail(
                event.getRecruiterEmail(),
                "Your job is live — " + event.getJobTitle(),
                html
        );
    }

    @RabbitListener(queues = "notification.application.interview.scheduled.queue")
    public void handleInterviewScheduled(InterviewScheduledDto event) {
        String name = (event.getApplicantName() == null || event.getApplicantName().isBlank())
                ? "Candidate" : event.getApplicantName();
        String html = templateService.interviewScheduled(
                name,
                event.getJobTitle(),
                event.getCompany(),
                event.getInterviewDate().toString(),
                event.getInterviewTime().toString(),
                event.getTimeZone(),
                event.getInterviewLink()
        );
        emailService.sendEmail(
                event.getUserEmail(),
                "Interview Confirmed — " + event.getJobTitle() + " at " + event.getCompany(),
                html
        );
    }

    @RabbitListener(queues = "notification.application.offer.sent.queue")
    public void handleOfferSent(OfferSentDto event) {
        String name = (event.getApplicantName() == null || event.getApplicantName().isBlank())
                ? "Candidate" : event.getApplicantName();
        String html = templateService.offerSent(
                name,
                event.getJobTitle(),
                event.getCompany(),
                event.getStatus(),
                event.getOfferLetterUrl()
        );
        emailService.sendEmail(
                event.getUserEmail(),
                "You have an offer — " + event.getJobTitle() + " at " + event.getCompany(),
                html
        );
    }

    @RabbitListener(queues = "notification.user.password.reset.queue")
    public void handleForgotPassword(ForgotPasswordEvent event) {
        String displayName = (event.getName() == null || event.getName().isBlank()) ? "User" : event.getName();
        String html = templateService.forgotPassword(displayName, event.getResetLink());
        emailService.sendEmail(
                event.getEmail(),
                "Reset your password",
                html
        );
    }
}
