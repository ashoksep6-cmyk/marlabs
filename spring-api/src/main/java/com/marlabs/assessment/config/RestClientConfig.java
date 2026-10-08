package com.marlabs.assessment.config;

import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestClient;

@Configuration
@EnableConfigurationProperties(CallerProperties.class)
public class RestClientConfig {
    @Bean RestClient pythonRestClient(RestClient.Builder builder) { return builder.build(); }
}
