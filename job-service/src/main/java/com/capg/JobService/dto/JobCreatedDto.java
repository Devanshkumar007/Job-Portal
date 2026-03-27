package com.capg.JobService.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class JobCreatedDto {
    private String jobTitle;
    private String recruiterEmail;
}
