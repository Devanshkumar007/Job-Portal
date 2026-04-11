package com.capg.ApplicationService.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class DashboardPipelineDto {
    private int applicationsReceivedPercent;
    private int interviewsScheduledPercent;
    private int offersSentPercent;
}
