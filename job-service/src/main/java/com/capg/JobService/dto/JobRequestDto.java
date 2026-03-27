package com.capg.JobService.dto;

import jakarta.persistence.Column;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.time.LocalDateTime;

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
}

