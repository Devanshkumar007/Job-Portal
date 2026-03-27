package com.capg.AdminService.config;

import feign.RequestInterceptor;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpHeaders;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.util.List;

@Configuration
public class FeignHeaderPropagationConfig {

    private static final List<String> HEADERS_TO_PROPAGATE = List.of(
            HttpHeaders.AUTHORIZATION,
            "X-User-Id",
            "X-User-Role"
    );

    @Bean
    public RequestInterceptor forwardIncomingHeaders() {
        return requestTemplate -> {
            ServletRequestAttributes attributes =
                    (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();

            if (attributes == null) {
                return;
            }

            HttpServletRequest request = attributes.getRequest();

            for (String header : HEADERS_TO_PROPAGATE) {
                if (requestTemplate.headers().containsKey(header)) {
                    continue;
                }

                String value = request.getHeader(header);
                if (value != null && !value.isBlank()) {
                    requestTemplate.header(header, value);
                }
            }
        };
    }
}
