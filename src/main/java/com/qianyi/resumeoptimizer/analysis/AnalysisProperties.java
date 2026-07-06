package com.qianyi.resumeoptimizer.analysis;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "app.analysis")
public record AnalysisProperties(
        String defaultBaseUrl,
        String defaultModel,
        int maxResumeChars,
        int maxJobDescriptionChars,
        String usageLogFile
) {
}

