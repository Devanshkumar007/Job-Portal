package com.capg.AdminService.service;

import com.capg.AdminService.client.JobsClient;
import com.capg.AdminService.dto.JobResponseDto;
import com.capg.AdminService.dto.PagedResponse;
import com.capg.AdminService.exception.DownstreamServiceUnavailableException;
import com.capg.AdminService.exception.UnauthorizedException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertSame;
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

    @Test
    @DisplayName("getAllJobs fallback should rethrow UnauthorizedException")
    void getAllJobsFallback_ShouldRethrowUnauthorizedException() {
        UnauthorizedException unauthorized = new UnauthorizedException("forbidden");

        RuntimeException thrown = invokeFallbackAndCapture(
                "getAllJobsFallback",
                new Class<?>[]{String.class, int.class, int.class, String.class, String.class, Throwable.class},
                "ADMIN", 0, 10, "createdAt", "desc", unauthorized
        );

        assertSame(unauthorized, thrown);
    }

    @Test
    @DisplayName("getJobById fallback should wrap downstream errors")
    void getJobByIdFallback_ShouldWrapDownstreamError() {
        RuntimeException downstream = new RuntimeException("service down");

        RuntimeException thrown = invokeFallbackAndCapture(
                "getJobByIdFallback",
                new Class<?>[]{String.class, Long.class, Throwable.class},
                "ADMIN", 33L, downstream
        );

        DownstreamServiceUnavailableException wrapped =
                assertInstanceOf(DownstreamServiceUnavailableException.class, thrown);
        assertEquals("Job service unreachable. Please try again after some time.", wrapped.getMessage());
        assertSame(downstream, wrapped.getCause());
    }

    @Test
    @DisplayName("deleteJob fallback should rethrow IllegalStateException")
    void deleteJobFallback_ShouldRethrowIllegalStateException() {
        IllegalStateException invalidState = new IllegalStateException("Recruiter id is missing for job: 33");

        RuntimeException thrown = invokeFallbackAndCapture(
                "deleteJobFallback",
                new Class<?>[]{String.class, Long.class, Throwable.class},
                "ADMIN", 33L, invalidState
        );

        assertSame(invalidState, thrown);
    }

    @Test
    @DisplayName("deleteJob fallback should wrap unexpected downstream errors")
    void deleteJobFallback_ShouldWrapDownstreamError() {
        RuntimeException downstream = new RuntimeException("timeout");

        RuntimeException thrown = invokeFallbackAndCapture(
                "deleteJobFallback",
                new Class<?>[]{String.class, Long.class, Throwable.class},
                "ADMIN", 33L, downstream
        );

        DownstreamServiceUnavailableException wrapped =
                assertInstanceOf(DownstreamServiceUnavailableException.class, thrown);
        assertEquals("Job service unreachable. Please try again after some time.", wrapped.getMessage());
        assertSame(downstream, wrapped.getCause());
    }

    @Test
    @DisplayName("getAllJobs fallback should wrap downstream errors")
    void getAllJobsFallback_ShouldWrapDownstreamError() {
        RuntimeException downstream = new RuntimeException("service down");

        RuntimeException thrown = invokeFallbackAndCapture(
                "getAllJobsFallback",
                new Class<?>[]{String.class, int.class, int.class, String.class, String.class, Throwable.class},
                "ADMIN", 0, 10, "createdAt", "desc", downstream
        );

        DownstreamServiceUnavailableException wrapped =
                assertInstanceOf(DownstreamServiceUnavailableException.class, thrown);
        assertEquals("Job service unreachable. Please try again after some time.", wrapped.getMessage());
        assertSame(downstream, wrapped.getCause());
    }

    @Test
    @DisplayName("getJobById fallback should rethrow UnauthorizedException")
    void getJobByIdFallback_ShouldRethrowUnauthorizedException() {
        UnauthorizedException unauthorized = new UnauthorizedException("forbidden");

        RuntimeException thrown = invokeFallbackAndCapture(
                "getJobByIdFallback",
                new Class<?>[]{String.class, Long.class, Throwable.class},
                "ADMIN", 33L, unauthorized
        );

        assertSame(unauthorized, thrown);
    }

    @Test
    @DisplayName("deleteJob fallback should rethrow UnauthorizedException")
    void deleteJobFallback_ShouldRethrowUnauthorizedException() {
        UnauthorizedException unauthorized = new UnauthorizedException("forbidden");

        RuntimeException thrown = invokeFallbackAndCapture(
                "deleteJobFallback",
                new Class<?>[]{String.class, Long.class, Throwable.class},
                "ADMIN", 33L, unauthorized
        );

        assertSame(unauthorized, thrown);
    }

    private RuntimeException invokeFallbackAndCapture(String methodName, Class<?>[] parameterTypes, Object... args) {
        try {
            Method method = AdminJobServiceImpl.class.getDeclaredMethod(methodName, parameterTypes);
            method.setAccessible(true);
            method.invoke(adminJobService, args);
            throw new AssertionError("Expected runtime exception from fallback method");
        } catch (InvocationTargetException ex) {
            Throwable cause = ex.getCause();
            if (cause instanceof RuntimeException runtimeException) {
                return runtimeException;
            }
            throw new RuntimeException(cause);
        } catch (ReflectiveOperationException ex) {
            throw new RuntimeException(ex);
        }
    }
}
