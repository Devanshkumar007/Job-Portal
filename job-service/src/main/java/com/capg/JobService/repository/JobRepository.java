package com.capg.JobService.repository;

import com.capg.JobService.entity.Job;
import com.capg.JobService.entity.JobStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface JobRepository extends JpaRepository<Job,Long>, JpaSpecificationExecutor<Job> {

    List<Job> findAllByRecruiterId(Long recruiterId);

    @Query("select j.id from Job j where j.recruiterId = :recruiterId")
    List<Long> findJobIdsByRecruiterId(@Param("recruiterId") Long recruiterId);

    long countByRecruiterId(Long recruiterId);

    long countByRecruiterIdAndStatus(Long recruiterId, JobStatus status);

    List<Job> findByRecruiterId(Long recruiterId, Pageable pageable);

    void deleteByRecruiterId(Long recruiterId);
}
