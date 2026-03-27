package com.capg.AdminService.service;

import com.capg.AdminService.client.ApplicationsClient;
import com.capg.AdminService.dto.ApplicationResponse;
import com.capg.AdminService.exception.DownstreamServiceUnavailableException;
import com.capg.AdminService.exception.UnauthorizedException;
import com.capg.AdminService.util.AdminRoleValidator;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class AdminApplicationServiceImpl implements AdminApplicationService {

    private final ApplicationsClient applicationsClient;

    @Override
    @CircuitBreaker(name = "applicationService", fallbackMethod = "getAllApplicationsFallback")
    public List<ApplicationResponse> getAllApplications(Long requesterId, String role) {
        AdminRoleValidator.requireAdmin(role);
        return applicationsClient.getAllApplications(requesterId, role);
    }

    @Override
    @CircuitBreaker(name = "applicationService", fallbackMethod = "getApplicationByIdFallback")
    public ApplicationResponse getApplicationById(Long applicationId, Long requesterId, String role) {
        AdminRoleValidator.requireAdmin(role);
        return applicationsClient.getApplicationById(applicationId, requesterId, role);
    }

    @Override
    @CircuitBreaker(name = "applicationService", fallbackMethod = "deleteApplicationFallback")
    public void deleteApplication(Long applicationId, Long requesterId, String role) {
        AdminRoleValidator.requireAdmin(role);
        applicationsClient.deleteApplication(applicationId, requesterId, role);
    }

    private List<ApplicationResponse> getAllApplicationsFallback(Long requesterId, String role, Throwable ex) {
        if (ex instanceof UnauthorizedException unauthorizedException) {
            throw unauthorizedException;
        }
        log.error("Fallback triggered. service=Application service, operation=getAllApplications, cause={}",
                ex.getMessage(), ex);
        throw new DownstreamServiceUnavailableException(
                "Application service unreachable. Please try again after some time.", ex);
    }

    private ApplicationResponse getApplicationByIdFallback(
            Long applicationId, Long requesterId, String role, Throwable ex) {
        if (ex instanceof UnauthorizedException unauthorizedException) {
            throw unauthorizedException;
        }
        log.error("Fallback triggered. service=Application service, operation=getApplicationById, cause={}",
                ex.getMessage(), ex);
        throw new DownstreamServiceUnavailableException(
                "Application service unreachable. Please try again after some time.", ex);
    }

    private void deleteApplicationFallback(Long applicationId, Long requesterId, String role, Throwable ex) {
        if (ex instanceof UnauthorizedException unauthorizedException) {
            throw unauthorizedException;
        }
        log.error("Fallback triggered. service=Application service, operation=deleteApplication, cause={}",
                ex.getMessage(), ex);
        throw new DownstreamServiceUnavailableException(
                "Application service unreachable. Please try again after some time.", ex);
    }
}
