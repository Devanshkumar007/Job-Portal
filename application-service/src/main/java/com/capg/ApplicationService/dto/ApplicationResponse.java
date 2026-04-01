package com.capg.ApplicationService.dto;

import com.capg.ApplicationService.entity.ApplicationStatus;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

@Data
public class ApplicationResponse implements Serializable {
    private Long id;
    private Long userId;
    private Long jobId;
    private String jobTitle;
    private String applicantEmail;
    private String resumeUrl;
    private String company;
    private ApplicationStatus status;
    private LocalDateTime appliedAt;
}
