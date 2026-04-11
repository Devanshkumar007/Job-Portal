package com.capg.ApplicationService.service;

import com.capg.ApplicationService.dto.ApplicationResponse;
import com.capg.ApplicationService.entity.Application;
import com.capg.ApplicationService.exception.ResourceNotFoundException;
import com.capg.ApplicationService.exception.UnauthorizedException;
import com.capg.ApplicationService.repository.ApplicationRepository;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.modelmapper.ModelMapper;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class AdminApplicationServiceImplTest {

    @Mock
    private ApplicationRepository repository;

    @Mock
    private ModelMapper modelMapper;

    @Mock
    private CloudinaryService cloudinaryService;

    @InjectMocks
    private AdminApplicationServiceImpl service;

    @Test
    @Order(1)
    void getAllApplications_as_admin_returns_mapped_list() {
        Application app = new Application();
        app.setId(1L);
        ApplicationResponse response = new ApplicationResponse();
        response.setId(1L);
        Pageable pageable = PageRequest.of(0, 10, Sort.by(Sort.Direction.DESC, "appliedAt"));

        when(repository.findAll(pageable)).thenReturn(new PageImpl<>(List.of(app), pageable, 1));
        when(modelMapper.map(app, ApplicationResponse.class)).thenReturn(response);

        Page<ApplicationResponse> result = service.getAllApplications(10L, "ADMIN", pageable);

        assertEquals(1, result.getTotalElements());
        assertEquals(1L, result.getContent().get(0).getId());

        ArgumentCaptor<Pageable> pageableCaptor = ArgumentCaptor.forClass(Pageable.class);
        verify(repository).findAll(pageableCaptor.capture());
        Sort.Order order = pageableCaptor.getValue().getSort().getOrderFor("appliedAt");
        assertEquals(Sort.Direction.DESC, order.getDirection());
    }

    @Test
    @Order(2)
    void getAllApplications_non_admin_throws_unauthorized() {
        Pageable pageable = PageRequest.of(0, 10);
        assertThrows(UnauthorizedException.class,
                () -> service.getAllApplications(10L, "RECRUITER", pageable));
        verify(repository, never()).findAll(any(Pageable.class));
    }

    @Test
    @Order(3)
    void getApplicationById_as_admin_returns_mapped_response() {
        Application app = new Application();
        app.setId(2L);
        ApplicationResponse response = new ApplicationResponse();
        response.setId(2L);

        when(repository.findById(2L)).thenReturn(Optional.of(app));
        when(modelMapper.map(app, ApplicationResponse.class)).thenReturn(response);

        ApplicationResponse result = service.getApplicationById(2L, 10L, "ADMIN");

        assertEquals(2L, result.getId());
    }

    @Test
    @Order(4)
    void getApplicationById_when_missing_throws_not_found() {
        when(repository.findById(2L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class,
                () -> service.getApplicationById(2L, 10L, "ADMIN"));
    }

    @Test
    @Order(5)
    void deleteApplication_as_admin_deletes_record() {
        Application app = new Application();
        app.setId(3L);
        when(repository.findById(3L)).thenReturn(Optional.of(app));

        service.deleteApplication(3L, 10L, "ADMIN");

        verify(repository).delete(app);
    }

    @Test
    @Order(8)
    void deleteApplication_with_offer_letter_deletes_cloudinary_asset() {
        Application app = new Application();
        app.setId(4L);
        app.setOfferLetterPublicId("offer/123");
        when(repository.findById(4L)).thenReturn(Optional.of(app));

        service.deleteApplication(4L, 10L, "ADMIN");

        verify(cloudinaryService).deleteOfferLetter("offer/123");
        verify(repository).delete(app);
    }

    @Test
    @Order(6)
    void deleteApplication_when_missing_throws_not_found() {
        when(repository.findById(3L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class,
                () -> service.deleteApplication(3L, 10L, "ADMIN"));

        verify(repository, never()).delete(any());
    }

    @Test
    @Order(7)
    void deleteApplication_non_admin_throws_unauthorized() {
        assertThrows(UnauthorizedException.class,
                () -> service.deleteApplication(3L, 10L, "JOB_SEEKER"));
        verify(repository, never()).findById(any());
    }
}
