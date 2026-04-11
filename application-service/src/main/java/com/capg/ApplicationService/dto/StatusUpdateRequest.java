package com.capg.ApplicationService.dto;

import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalTime;

@Data
@NoArgsConstructor
public class StatusUpdateRequest {
    private Long applicationId;
    private String company;
    private String status;
    private String interviewLink;
    private LocalDate interviewDate;
    private LocalTime interviewTime;
    private String timeZone;

    public StatusUpdateRequest(Long applicationId, String company, String status) {
        this.applicationId = applicationId;
        this.company = company;
        this.status = status;
    }
}
