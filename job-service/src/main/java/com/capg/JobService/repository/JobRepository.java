package com.capg.JobService.repository;

import com.capg.JobService.entity.Job;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.List;

public interface JobRepository extends JpaRepository<Job,Long>, JpaSpecificationExecutor<Job> {

    List<Job> findAllByRecruiterId(Long recruiterId);

    void deleteByRecruiterId(Long recruiterId);
}
