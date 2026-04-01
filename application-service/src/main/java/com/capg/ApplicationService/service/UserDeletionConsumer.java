package com.capg.ApplicationService.service;

import com.capg.ApplicationService.dto.UserDeletedEvent;
import com.capg.ApplicationService.entity.Application;
import com.capg.ApplicationService.repository.ApplicationRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@RequiredArgsConstructor
public class UserDeletionConsumer {

    private final ApplicationRepository applicationRepository;
    private final CloudinaryService cloudinaryService;

    @RabbitListener(queues = "application.user.deleted.queue")
    @Transactional
    public void handleUserDeleted(UserDeletedEvent event) {

        if ("JOB_SEEKER".equals(event.getRole())) {
            List<Application> applications = applicationRepository.findByUserId(event.getUserId());
            applications.forEach(app -> {
                if (app.getResumePublicId() != null) {
                    cloudinaryService.deleteResume(app.getResumePublicId());
                }
            });
            applicationRepository.deleteByUserId(event.getUserId());
            System.out.println("Deleted applications for user: " + event.getUserId());
        }
    }
}
