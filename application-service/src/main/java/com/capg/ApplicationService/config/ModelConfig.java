package com.capg.ApplicationService.config;

import com.capg.ApplicationService.dto.ApplicationRequest;
import com.capg.ApplicationService.entity.Application;
import org.modelmapper.ModelMapper;
import org.modelmapper.convention.MatchingStrategies;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class ModelConfig {
    @Bean
    public ModelMapper modelMapper(){
        ModelMapper mapper = new ModelMapper();
        mapper.getConfiguration()
                .setMatchingStrategy(MatchingStrategies.STRICT)
                .setAmbiguityIgnored(true);
        mapper.typeMap(ApplicationRequest.class, Application.class)
                .addMappings(m -> { m.map(ApplicationRequest::getUserId, Application::setUserId);
                    m.map(ApplicationRequest::getJobId, Application::setJobId);
                    m.map(ApplicationRequest::getResumeUrl, Application::setResumeUrl);
                    m.skip(Application::setId); });
        return mapper; }
}
