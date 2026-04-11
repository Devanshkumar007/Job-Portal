package com.capg.JobService.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Data;

@Data
public class JobRequestDto {
    @NotBlank
    private String title;
    @NotBlank
    private String companyName;
    @NotBlank
    private String location;
    @NotNull
    private Double salary;
    @NotNull
    private Integer experience;
    @NotBlank
    private String description;
    @NotBlank
    private String recruiterEmail;
    @NotBlank
    private String jobType;
    @Positive
    private Integer internshipDurationMonths;
}

