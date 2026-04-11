package com.capg.AdminService.service;

import com.capg.AdminService.client.ApplicationsClient;
import com.capg.AdminService.dto.ApplicationResponse;
import com.capg.AdminService.dto.PagedResponse;
import com.capg.AdminService.exception.UnauthorizedException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
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
    @DisplayName("getAllApplications should return paged applications when requester role is ADMIN")
    void getAllApplications_ShouldReturnApplications_WhenRoleIsAdmin() {
        Long requesterId = 10L;
        String role = "ADMIN";
        int page = 0;
        int size = 10;
        String sortBy = "appliedAt";
        String direction = "desc";
        String sort = "appliedAt,desc";
        PagedResponse<ApplicationResponse> expected = new PagedResponse<>();
        expected.setNumber(page);
        expected.setSize(size);

        when(applicationsClient.getAllApplications(page, size, sort, requesterId, role)).thenReturn(expected);

        PagedResponse<ApplicationResponse> actual =
                adminApplicationService.getAllApplications(requesterId, role, page, size, sortBy, direction);

        assertEquals(expected, actual);
        verify(applicationsClient).getAllApplications(page, size, sort, requesterId, role);
    }

    @Test
    @DisplayName("getAllApplications should reject null role and avoid downstream client call")
    void getAllApplications_ShouldThrowUnauthorized_WhenRoleIsNull() {
        Long requesterId = 10L;
        int page = 0;
        int size = 10;
        String sortBy = "appliedAt";
        String direction = "desc";

        assertThrows(UnauthorizedException.class,
                () -> adminApplicationService.getAllApplications(requesterId, null, page, size, sortBy, direction));
        verify(applicationsClient, never()).getAllApplications(page, size, "appliedAt,desc", requesterId, null);
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
