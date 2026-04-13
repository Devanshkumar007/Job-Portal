package com.capg.ApplicationService.controller;

import com.capg.ApplicationService.dto.ApplicationResponse;
import com.capg.ApplicationService.exception.GlobalExceptionHandler;
import com.capg.ApplicationService.service.AdminApplicationService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.PageImpl;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = AdminApplicationController.class)
@Import(GlobalExceptionHandler.class)
class AdminApplicationControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private AdminApplicationService adminApplicationService;

    @Test
    void get_all_applications_returns_ok() throws Exception {
        ApplicationResponse response = new ApplicationResponse();
        response.setId(1L);
        when(adminApplicationService.getAllApplications(eq(10L), eq("ADMIN"), any()))
                .thenReturn(new PageImpl<>(List.of(response)));

        mockMvc.perform(get("/api/admin/applications")
                        .header("X-User-Id", "10")
                        .header("X-User-Role", "ADMIN"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].id").value(1));
    }

    @Test
    void get_application_by_id_returns_ok() throws Exception {
        ApplicationResponse response = new ApplicationResponse();
        response.setId(2L);
        when(adminApplicationService.getApplicationById(2L, 10L, "ADMIN")).thenReturn(response);

        mockMvc.perform(get("/api/admin/applications/2")
                        .header("X-User-Id", "10")
                        .header("X-User-Role", "ADMIN"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(2));
    }

    @Test
    void delete_application_returns_ok() throws Exception {
        doNothing().when(adminApplicationService).deleteApplication(3L, 10L, "ADMIN");

        mockMvc.perform(delete("/api/admin/applications/3")
                        .header("X-User-Id", "10")
                        .header("X-User-Role", "ADMIN"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Application deleted successfully"));
    }
}

