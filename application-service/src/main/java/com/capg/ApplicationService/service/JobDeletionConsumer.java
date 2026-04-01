package com.capg.ApplicationService.service;

import com.capg.ApplicationService.dto.JobDeletedEvent;
import com.capg.ApplicationService.entity.Application;
import com.capg.ApplicationService.repository.ApplicationRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@RequiredArgsConstructor
public class JobDeletionConsumer {

    private final ApplicationRepository repository;
    private final CloudinaryService cloudinaryService;
    private final ApplicationRepository applicationRepository;

    @RabbitListener(queues = "application.job.deleted.queue")
    @Transactional
    public void handleJobDeleted(JobDeletedEvent event) {

        List<Application> applications = applicationRepository.findByJobId(event.getJobId());

        applications.forEach(app -> {
            if (app.getResumePublicId() != null) {
                cloudinaryService.deleteResume(app.getResumePublicId());
            }
        });

        repository.deleteByJobId(event.getJobId());
        System.out.println("Deleted applications for job: " + event.getJobId());
    }
}
