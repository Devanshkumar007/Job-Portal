package com.capg.ApplicationService.dto;

import com.capg.ApplicationService.entity.ApplicationStatus;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class StatusUpdateRequest {
    private Long applicationId;
    private String company;
    private String status;
}
