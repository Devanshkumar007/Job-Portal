package com.capg.JobService.specification;

import com.capg.JobService.dto.JobFilterDto;
import com.capg.JobService.entity.Job;
import org.junit.jupiter.api.Test;
import org.springframework.data.jpa.domain.Specification;

import static org.junit.jupiter.api.Assertions.assertNotNull;

class JobSpecificationTest {

    @Test
    void getFilteredJobs_shouldReturnSpecification_forEmptyFilter() {
        JobFilterDto filter = new JobFilterDto();

        Specification<Job> specification = JobSpecification.getFilteredJobs(filter);

        assertNotNull(specification);
    }

    @Test
    void getFilteredJobs_shouldReturnSpecification_forPopulatedFilter() {
        JobFilterDto filter = new JobFilterDto();
        filter.setTitle("Developer");
        filter.setLocation("Pune");
        filter.setCompanyName("Acme");
        filter.setMinSalary(5.0);
        filter.setMaxSalary(20.0);
        filter.setMinExperience(1);
        filter.setMaxExperience(8);
        filter.setJobType("FULL_TIME");

        Specification<Job> specification = JobSpecification.getFilteredJobs(filter);

        assertNotNull(specification);
    }
}
