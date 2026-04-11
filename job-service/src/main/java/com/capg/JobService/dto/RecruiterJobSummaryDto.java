package com.capg.JobService.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class RecruiterJobSummaryDto {
    private long totalJobs;
    private long activeJobs;
    private long draftJobs;
    private long closedJobs;
}
