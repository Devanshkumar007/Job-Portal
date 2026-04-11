package com.capg.ApplicationService.dto;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class JobDetailsResponse {
    private Long id;
    private Long recruiterId;
    private String title;
    private String companyName;
    private LocalDateTime createdAt;
}
