package com.capg.AdminService.service;

import com.capg.AdminService.client.JobsClient;
import com.capg.AdminService.dto.JobResponseDto;
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
class AdminJobServiceImplTest {

    @Mock
    private JobsClient jobsClient;

    @InjectMocks
    private AdminJobServiceImpl adminJobService;

    @Test
    @DisplayName("getAllJobs should return paged jobs when role is ADMIN")
    void getAllJobs_ShouldReturnPagedResponse_WhenRoleIsAdmin() {
        PagedResponse<JobResponseDto> expected = new PagedResponse<>();

        when(jobsClient.getAllJobs(0, 10, "createdAt", "desc")).thenReturn(expected);

        PagedResponse<JobResponseDto> actual =
                adminJobService.getAllJobs("ADMIN", 0, 10, "createdAt", "desc");

        assertEquals(expected, actual);
        verify(jobsClient).getAllJobs(0, 10, "createdAt", "desc");
    }

    @Test
    @DisplayName("getJobById should throw UnauthorizedException for non-admin role")
    void getJobById_ShouldThrowUnauthorized_WhenRoleIsNotAdmin() {
        Long jobId = 22L;

        assertThrows(UnauthorizedException.class, () -> adminJobService.getJobById("USER", jobId));
        verify(jobsClient, never()).getJobById(jobId);
    }

    @Test
    @DisplayName("getJobById should return job details for case-insensitive admin role")
    void getJobById_ShouldReturnJob_WhenRoleIsLowercaseAdmin() {
        Long jobId = 22L;
        JobResponseDto expected = new JobResponseDto();
        expected.setId(jobId);

        when(jobsClient.getJobById(jobId)).thenReturn(expected);

        JobResponseDto actual = adminJobService.getJobById("admin", jobId);

        assertEquals(expected, actual);
        verify(jobsClient).getJobById(jobId);
    }

    @Test
    @DisplayName("deleteJob should call delete with recruiter id fetched from job details")
    void deleteJob_ShouldDeleteUsingRecruiterId_WhenRecruiterIdIsPresent() {
        Long jobId = 33L;
        Long recruiterId = 99L;
        JobResponseDto job = new JobResponseDto();
        job.setId(jobId);
        job.setRecruiterId(recruiterId);

        when(jobsClient.getJobById(jobId)).thenReturn(job);

        adminJobService.deleteJob("ADMIN", jobId);

        verify(jobsClient).getJobById(jobId);
        verify(jobsClient).deleteJob(jobId, recruiterId);
    }

    @Test
    @DisplayName("deleteJob should throw IllegalStateException when recruiter id is missing")
    void deleteJob_ShouldThrowIllegalStateException_WhenRecruiterIdIsNull() {
        Long jobId = 33L;
        JobResponseDto job = new JobResponseDto();
        job.setId(jobId);
        job.setRecruiterId(null);

        when(jobsClient.getJobById(jobId)).thenReturn(job);

        IllegalStateException exception = assertThrows(IllegalStateException.class,
                () -> adminJobService.deleteJob("ADMIN", jobId));
        assertEquals("Recruiter id is missing for job: 33", exception.getMessage());
        verify(jobsClient).getJobById(jobId);
        verify(jobsClient, never()).deleteJob(jobId, null);
    }

    @Test
    @DisplayName("deleteJob should fail fast when downstream returns null job payload")
    void deleteJob_ShouldThrowNullPointerException_WhenJobPayloadIsNull() {
        Long jobId = 33L;
        when(jobsClient.getJobById(jobId)).thenReturn(null);

        assertThrows(NullPointerException.class, () -> adminJobService.deleteJob("ADMIN", jobId));
        verify(jobsClient).getJobById(jobId);
        verify(jobsClient, never()).deleteJob(jobId, null);
    }

    @Test
    @DisplayName("deleteJob should reject non-admin role and not call job client")
    void deleteJob_ShouldThrowUnauthorized_WhenRoleIsNotAdmin() {
        Long jobId = 33L;

        assertThrows(UnauthorizedException.class, () -> adminJobService.deleteJob("RECRUITER", jobId));
        verify(jobsClient, never()).getJobById(jobId);
        verify(jobsClient, never()).deleteJob(jobId, null);
    }
}
