package com.capg.ApplicationService.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class InterviewScheduledDto {
    private String userEmail;
    private String applicantName;
    private String jobTitle;
    private String company;
    private String interviewLink;
    private LocalDate interviewDate;
    private LocalTime interviewTime;
    private String timeZone;
}
