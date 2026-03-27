package com.capg.AdminService.service;

import com.capg.AdminService.client.ApplicationsClient;
import com.capg.AdminService.dto.ApplicationResponse;
import com.capg.AdminService.exception.UnauthorizedException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AdminApplicationServiceImplTest {

    @Mock
    private ApplicationsClient applicationsClient;

    @InjectMocks
    private AdminApplicationServiceImpl adminApplicationService;

    @Test
    @DisplayName("getAllApplications should return application list when requester role is ADMIN")
    void getAllApplications_ShouldReturnApplications_WhenRoleIsAdmin() {
        Long requesterId = 10L;
        String role = "ADMIN";
        ApplicationResponse first = new ApplicationResponse();
        first.setId(1L);
        ApplicationResponse second = new ApplicationResponse();
        second.setId(2L);
        List<ApplicationResponse> expected = List.of(first, second);

        when(applicationsClient.getAllApplications(requesterId, role)).thenReturn(expected);

        List<ApplicationResponse> actual = adminApplicationService.getAllApplications(requesterId, role);

        assertEquals(expected, actual);
        verify(applicationsClient).getAllApplications(requesterId, role);
    }

    @Test
    @DisplayName("getAllApplications should reject null role and avoid downstream client call")
    void getAllApplications_ShouldThrowUnauthorized_WhenRoleIsNull() {
        Long requesterId = 10L;

        assertThrows(UnauthorizedException.class,
                () -> adminApplicationService.getAllApplications(requesterId, null));
        verify(applicationsClient, never()).getAllApplications(requesterId, null);
    }

    @Test
    @DisplayName("getApplicationById should allow case-insensitive admin role and fetch the application")
    void getApplicationById_ShouldAllowCaseInsensitiveAdminRole() {
        Long applicationId = 45L;
        Long requesterId = 10L;
        String role = "admin";
        ApplicationResponse expected = new ApplicationResponse();
        expected.setId(applicationId);

        when(applicationsClient.getApplicationById(applicationId, requesterId, role)).thenReturn(expected);

        ApplicationResponse actual =
                adminApplicationService.getApplicationById(applicationId, requesterId, role);

        assertEquals(expected, actual);
        verify(applicationsClient).getApplicationById(applicationId, requesterId, role);
    }

    @Test
    @DisplayName("deleteApplication should delegate to client when requester role is ADMIN")
    void deleteApplication_ShouldCallClient_WhenRoleIsAdmin() {
        Long applicationId = 45L;
        Long requesterId = 10L;
        String role = "ADMIN";

        adminApplicationService.deleteApplication(applicationId, requesterId, role);

        verify(applicationsClient).deleteApplication(applicationId, requesterId, role);
    }

    @Test
    @DisplayName("deleteApplication should throw UnauthorizedException for non-admin role and skip delete")
    void deleteApplication_ShouldThrowUnauthorized_WhenRoleIsNotAdmin() {
        Long applicationId = 45L;
        Long requesterId = 10L;
        String role = "RECRUITER";

        assertThrows(UnauthorizedException.class,
                () -> adminApplicationService.deleteApplication(applicationId, requesterId, role));
        verify(applicationsClient, never()).deleteApplication(applicationId, requesterId, role);
    }
}
