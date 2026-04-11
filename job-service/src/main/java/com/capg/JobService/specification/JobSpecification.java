package com.capg.JobService.specification;

import com.capg.JobService.dto.JobFilterDto;
import com.capg.JobService.entity.Job;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.jpa.domain.Specification;

import java.util.ArrayList;
import java.util.List;


public class JobSpecification {

    public static Specification<Job> getFilteredJobs(JobFilterDto filter) {

        return (root, query, cb) -> {

            List<Predicate> predicates = new ArrayList<>();

            if (filter.getTitle() != null) {
                predicates.add(cb.like(cb.lower(root.get("title")),
                        "%" + filter.getTitle().toLowerCase() + "%"));
            }

            if (filter.getLocation() != null) {
                predicates.add(cb.like(cb.lower(root.get("location")),
                        "%" + filter.getLocation().toLowerCase() + "%"));
            }

            if (filter.getCompanyName() != null) {
                predicates.add(cb.like(cb.lower(root.get("companyName")),
                        "%" + filter.getCompanyName().toLowerCase() + "%"));
            }

            if (filter.getMinSalary() != null) {
                predicates.add(cb.greaterThanOrEqualTo(root.get("salary"), filter.getMinSalary()));
            }

            if (filter.getMaxSalary() != null) {
                predicates.add(cb.lessThanOrEqualTo(root.get("salary"), filter.getMaxSalary()));
            }

            if (filter.getMinExperience() != null) {
                predicates.add(cb.greaterThanOrEqualTo(root.get("experience"), filter.getMinExperience()));
            }

            if (filter.getMaxExperience() != null) {
                predicates.add(cb.lessThanOrEqualTo(root.get("experience"), filter.getMaxExperience()));
            }

            if (filter.getJobType() != null) {
                predicates.add(cb.equal(
                        cb.lower(root.get("jobType").as(String.class)),
                        filter.getJobType().toLowerCase()
                ));
            }

            return cb.and(predicates.toArray(new Predicate[0]));
        };
    }
}
