package com.capg.ApplicationService.dto;

import lombok.Data;

@Data
public class ApplicationRequest {
    private Long userId;
    private Long jobId;
    private String resumeUrl;
    private String applicantEmail;
    private String applicantName;
    private String jobTitle;
    private String company;
    private String resumePublicId;
}
