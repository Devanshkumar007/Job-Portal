package com.capg.JobService.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class Job {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String title;
    private String companyName;
    private String location;
    private Double salary;
    private Integer experience;

    @Column(length = 2000)
    private String description;

    @Enumerated(EnumType.STRING)
    private JobType jobType;

    private Integer internshipDurationMonths;

    @Enumerated(EnumType.STRING)
    private JobStatus status;

//    @Column(name = "recruiter_id")
    private Long recruiterId;
    private LocalDateTime createdAt;

    @PrePersist
    public void applyDefaults() {
        if (status == null) {
            status = JobStatus.ACTIVE;
        }
    }
}
