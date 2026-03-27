package com.capg.AdminService.dto;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class ApplicationResponse {
    private Long id;
    private Long userId;
    private Long jobId;
    private String resumeUrl;
    private String status;
    private LocalDateTime appliedAt;
}
