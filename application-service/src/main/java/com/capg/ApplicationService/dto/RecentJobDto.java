package com.capg.ApplicationService.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class RecentJobDto {
    private Long jobId;
    private String title;
    private String companyName;
    private String status;
}
