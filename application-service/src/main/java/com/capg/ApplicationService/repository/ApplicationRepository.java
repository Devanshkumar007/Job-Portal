package com.capg.ApplicationService.repository;

import com.capg.ApplicationService.entity.Application;
import com.capg.ApplicationService.entity.ApplicationStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface ApplicationRepository extends JpaRepository<Application, Long> {
    List<Application> findByUserId(Long userId);

    Page<Application> findByUserId(Long userId, Pageable pageable);
    Page<Application> findByUserIdAndStatus(Long userId, ApplicationStatus status, Pageable pageable);

    List<Application> findByJobId(Long jobId);

    Page<Application> findByJobId(Long jobId, Pageable pageable);

    boolean existsByUserIdAndJobId(Long userId, Long jobId);

    void deleteByUserId(Long userId);

    void deleteByJobId(Long jobId);

    long countByJobIdIn(List<Long> jobIds);

    long countByJobIdInAndStatus(List<Long> jobIds, ApplicationStatus status);

    List<Application> findTop3ByJobIdInOrderByAppliedAtDesc(List<Long> jobIds);
}
