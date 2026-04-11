package com.capg.ApplicationService.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.time.LocalDate;
import java.time.LocalTime;


@Table(name = "applications", uniqueConstraints = {
        @UniqueConstraint(columnNames = {"user_id", "job_id"})
})
@Data
@AllArgsConstructor
@NoArgsConstructor
@Entity
public class Application {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name= "user_id", nullable = false)
    private Long userId;

    @Column(name = "job_id", nullable = false)
    private Long jobId;

    @Column(name = "resume_url")
    private String resumeUrl;

    @Enumerated(EnumType.STRING)
    private ApplicationStatus status;

    private LocalDateTime appliedAt;

    private String company;

    private String applicantEmail;

    private String applicantName;

    private String jobTitle;

    @Column(name = "resume_public_id")
    private String resumePublicId;

    @Column(name = "offer_letter_url")
    private String offerLetterUrl;

    @Column(name = "offer_letter_public_id")
    private String offerLetterPublicId;

    @Column(name = "interview_link")
    private String interviewLink;

    @Column(name = "interview_date")
    private LocalDate interviewDate;

    @Column(name = "interview_time")
    private LocalTime interviewTime;

    @Column(name = "interview_time_zone")
    private String interviewTimeZone;

}
