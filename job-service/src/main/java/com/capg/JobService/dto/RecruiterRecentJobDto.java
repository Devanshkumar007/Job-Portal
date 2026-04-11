package com.capg.JobService.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
public class RecruiterRecentJobDto {
    private Long jobId;
    private String title;
    private String companyName;
    private String status;
    private LocalDateTime createdAt;

    public RecruiterRecentJobDto(Long jobId, String title, String companyName, String status) {
        this(jobId, title, companyName, status, null);
    }
}
