package com.capg.NotificationService.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ApplicationStatusDto {
    private String userEmail;
    private String jobTitle;
    private String status;
    private String company;
}
