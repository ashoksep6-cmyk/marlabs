package com.marlabs.assessment.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import java.util.Map;

@ConfigurationProperties(prefix="assessment")
public record CallerProperties(Map<String, Caller> callers) {
    public record Caller(String tenant, String role) {}
}
