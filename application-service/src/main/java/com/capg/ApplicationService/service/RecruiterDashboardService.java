package com.capg.ApplicationService.service;

import com.capg.ApplicationService.dto.DashboardPipelineDto;
import com.capg.ApplicationService.dto.DashboardSummaryDto;
import com.capg.ApplicationService.dto.RecentCandidateDto;
import com.capg.ApplicationService.dto.RecentJobDto;
import com.capg.ApplicationService.dto.RecruiterDashboardDto;

import java.util.List;

public interface RecruiterDashboardService {
    DashboardSummaryDto getSummary(Long recruiterId, String role);

    List<RecentJobDto> getRecentJobs(Long recruiterId, String role);

    List<RecentCandidateDto> getRecentCandidates(Long recruiterId, String role);

    DashboardPipelineDto getPipeline(Long recruiterId, String role);

    RecruiterDashboardDto getDashboard(Long recruiterId, String role);
}
