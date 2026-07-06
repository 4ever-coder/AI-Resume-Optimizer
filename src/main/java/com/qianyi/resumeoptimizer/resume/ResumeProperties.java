package com.qianyi.resumeoptimizer.resume;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "app.resume")
public record ResumeProperties(
        long maxFileSizeBytes,
        int maxPdfPages,
        int minTextChars
) {
}

