package com.capg.ApplicationService.service;

import com.capg.ApplicationService.dto.ApplicationResponse;
import com.capg.ApplicationService.dto.JobDeletedEvent;
import com.capg.ApplicationService.entity.Application;
import com.capg.ApplicationService.exception.ResourceNotFoundException;
import com.capg.ApplicationService.exception.UnauthorizedException;
import com.capg.ApplicationService.repository.ApplicationRepository;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class AdminApplicationServiceImpl implements AdminApplicationService {

    private static final String ROLE_ADMIN = "ADMIN";

    private final ApplicationRepository repository;
    private final ModelMapper modelMapper;
    private final CloudinaryService cloudinaryService;

    @Override
    @Cacheable(value = "adminAllApplications", key = "#authenticatedUserId + ':' + #role")
    public List<ApplicationResponse> getAllApplications(Long authenticatedUserId, String role) {
        assertAdminRole(role);
        return repository.findAll(Sort.by(Sort.Direction.DESC, "appliedAt"))
                .stream()
                .map(this::toResponse)
                .toList();
    }

    @Override
    @Cacheable(value = "adminApplicationById", key = "#applicationId + ':' + #role")
    public ApplicationResponse getApplicationById(Long applicationId, Long authenticatedUserId, String role) {
        assertAdminRole(role);

        Application application = repository.findById(applicationId)
                .orElseThrow(() -> new ResourceNotFoundException("Application not found with id: " + applicationId));
        return toResponse(application);
    }

    @Override
    @CacheEvict(
            value = {"adminAllApplications", "adminApplicationById", "userApplications", "jobApplicants"},
            allEntries = true
    )
    public void deleteApplication(Long applicationId, Long authenticatedUserId, String role) {
        assertAdminRole(role);

        Application app = repository.findById(applicationId)
                .orElseThrow(() -> new ResourceNotFoundException("Application not found with id: " + applicationId));

        if (app.getResumePublicId() != null) {

            cloudinaryService.deleteResume(app.getResumePublicId());
        }

        repository.delete(app);

    }

    private ApplicationResponse toResponse(Application application) {
        return modelMapper.map(application, ApplicationResponse.class);
    }

    private void assertAdminRole(String role) {
        if (!ROLE_ADMIN.equalsIgnoreCase(role)) {
            throw new UnauthorizedException("Only admins can access this resource");
        }
    }
}
