package com.capg.ApplicationService.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class RecruiterDashboardDto {
    private DashboardSummaryDto summary;
    private List<RecentJobDto> recentJobs;
    private List<RecentCandidateDto> recentCandidates;
    private DashboardPipelineDto pipeline;
}
