package com.capg.JobService.service;

import com.capg.JobService.dto.UserDeletedEvent;
import com.capg.JobService.repository.JobRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class UserDeletionConsumer {

    private final JobRepository jobRepository;

    @RabbitListener(queues = "job.user.deleted.queue")
    @Transactional
    public void handleUserDeleted(UserDeletedEvent event) {

        if ("RECRUITER".equals(event.getRole())) {
            jobRepository.deleteByRecruiterId(event.getUserId());
            System.out.println("Deleted jobs for recruiter: " + event.getUserId());
        }
    }
}
