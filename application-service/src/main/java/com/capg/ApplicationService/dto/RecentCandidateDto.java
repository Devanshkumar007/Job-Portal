package com.capg.ApplicationService.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class RecentCandidateDto {
    private Long applicationId;
    private Long jobId;
    private String applicantName;
    private String applicantEmail;
    private String jobTitle;
    private String status;
}
