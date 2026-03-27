package com.capg.JobService.repository;

import com.capg.JobService.entity.Job;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.List;
import java.util.Optional;

public interface JobRepository extends JpaRepository<Job,Long>, JpaSpecificationExecutor<Job> {
//
//    List<Job> findByLocation(String location);
//    List<Job> findByTitle(String title);
//    List<Job> findByCompanyName(String companyName);

    void deleteByRecruiterId(Long recruiterId);

}
