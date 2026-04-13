package com.capg.ApplicationService.controller;

import com.capg.ApplicationService.dto.ApplicationResponse;
import com.capg.ApplicationService.dto.StatusUpdateRequest;
import com.capg.ApplicationService.entity.ApplicationStatus;
import com.capg.ApplicationService.exception.GlobalExceptionHandler;
import com.capg.ApplicationService.service.ApplicationService;
import com.capg.ApplicationService.service.CloudinaryService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.PageImpl;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = ApplicationController.class)
@Import(GlobalExceptionHandler.class)
class ApplicationControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private ApplicationService applicationService;

    @MockBean
    private CloudinaryService cloudinaryService;

    @Test
    void apply_returns_ok() throws Exception {
        ApplicationResponse response = new ApplicationResponse();
        response.setId(11L);

        when(cloudinaryService.uploadResume(any())).thenReturn(Map.of("url", "https://cdn/resume.pdf", "publicId", "resume-1"));
        when(applicationService.apply(any(), eq(77L), eq("JOB_SEEKER"))).thenReturn(response);

        MockMultipartFile file = new MockMultipartFile(
                "file",
                "resume.pdf",
                MediaType.APPLICATION_PDF_VALUE,
                "pdf".getBytes()
        );

        mockMvc.perform(multipart("/api/applications/apply")
                        .file(file)
                        .param("jobId", "201")
                        .param("applicantEmail", "a@test.com")
                        .param("applicantName", "Alex")
                        .param("jobTitle", "Backend Engineer")
                        .param("company", "Capgemini")
                        .header("X-User-Id", "77")
                        .header("X-User-Role", "JOB_SEEKER"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(11));

        ArgumentCaptor<com.capg.ApplicationService.dto.ApplicationRequest> requestCaptor =
                ArgumentCaptor.forClass(com.capg.ApplicationService.dto.ApplicationRequest.class);
        verify(applicationService).apply(requestCaptor.capture(), eq(77L), eq("JOB_SEEKER"));
        assertThat(requestCaptor.getValue().getResumeUrl()).isEqualTo("https://cdn/resume.pdf");
        assertThat(requestCaptor.getValue().getResumePublicId()).isEqualTo("resume-1");
    }

    @Test
    void get_user_apps_returns_ok() throws Exception {
        ApplicationResponse response = new ApplicationResponse();
        response.setId(1L);
        when(applicationService.getUserApplications(eq(77L), eq(77L), eq("JOB_SEEKER"), any()))
                .thenReturn(new PageImpl<>(List.of(response)));

        mockMvc.perform(get("/api/applications/user/77")
                        .header("X-User-Id", "77")
                        .header("X-User-Role", "JOB_SEEKER"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].id").value(1));
    }

    @Test
    void get_user_apps_by_status_returns_ok() throws Exception {
        ApplicationResponse response = new ApplicationResponse();
        response.setId(2L);
        response.setStatus(ApplicationStatus.APPLIED);
        when(applicationService.getUserApplicationsByStatus(eq(77L), eq("JOB_SEEKER"), eq(ApplicationStatus.APPLIED), any()))
                .thenReturn(new PageImpl<>(List.of(response)));

        mockMvc.perform(get("/api/applications/user/by-status")
                        .param("status", "applied")
                        .header("X-User-Id", "77")
                        .header("X-User-Role", "JOB_SEEKER"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].id").value(2))
                .andExpect(jsonPath("$.content[0].status").value("APPLIED"));
    }

    @Test
    void get_user_apps_by_status_with_invalid_status_returns_bad_request() throws Exception {
        mockMvc.perform(get("/api/applications/user/by-status")
                        .param("status", "bad_status")
                        .header("X-User-Id", "77")
                        .header("X-User-Role", "JOB_SEEKER"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$").value("Invalid status: bad_status"));
    }

    @Test
    void get_job_apps_returns_ok() throws Exception {
        ApplicationResponse response = new ApplicationResponse();
        response.setId(3L);
        when(applicationService.getJobApplicants(eq(1001L), eq(91L), eq("RECRUITER"), any()))
                .thenReturn(new PageImpl<>(List.of(response)));

        mockMvc.perform(get("/api/applications/job/1001")
                        .header("X-User-Id", "91")
                        .header("X-User-Role", "RECRUITER"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].id").value(3));
    }

    @Test
    void update_status_returns_ok() throws Exception {
        ApplicationResponse response = new ApplicationResponse();
        response.setId(5L);
        response.setStatus(ApplicationStatus.UNDER_REVIEW);
        when(applicationService.updateStatus(any(StatusUpdateRequest.class), eq(91L), eq("RECRUITER")))
                .thenReturn(response);

        StatusUpdateRequest request = new StatusUpdateRequest(5L, "Capgemini", "UNDER_REVIEW");

        mockMvc.perform(put("/api/applications/status")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request))
                        .header("X-User-Id", "91")
                        .header("X-User-Role", "RECRUITER"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(5))
                .andExpect(jsonPath("$.status").value("UNDER_REVIEW"));
    }

    @Test
    void update_status_to_offered_returns_ok() throws Exception {
        ApplicationResponse response = new ApplicationResponse();
        response.setId(6L);
        response.setStatus(ApplicationStatus.OFFERED);
        when(applicationService.updateStatusToOffered(eq(6L), eq("Capgemini"), any(), eq(91L), eq("RECRUITER")))
                .thenReturn(response);

        MockMultipartFile file = new MockMultipartFile(
                "file",
                "offer-letter.pdf",
                MediaType.APPLICATION_PDF_VALUE,
                "pdf".getBytes()
        );

        MockHttpServletRequestBuilder request = multipart("/api/applications/status/offered")
                .file(file)
                .param("applicationId", "6")
                .param("company", "Capgemini")
                .header("X-User-Id", "91")
                .header("X-User-Role", "RECRUITER");
        request.with(r -> {
            r.setMethod("PUT");
            return r;
        });

        mockMvc.perform(request)
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(6))
                .andExpect(jsonPath("$.status").value("OFFERED"));
    }
}

