package com.capg.ApplicationService.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class OfferSentDto {
    private Long applicationId;
    private String userEmail;
    private String applicantName;
    private String jobTitle;
    private String company;
    private String status;
    private String offerLetterUrl;
}
