package com.capg.ApplicationService.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class DashboardSummaryDto {
    private long openRoles;
    private long totalApplications;
    private long shortlistedCount;
    private long offersSent;
}
