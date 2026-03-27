package com.capg.NotificationService.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ApplicationCreatedDto {
    private String applicantEmail;
    private String jobTitle;
    private String company;
    private String status;
}
