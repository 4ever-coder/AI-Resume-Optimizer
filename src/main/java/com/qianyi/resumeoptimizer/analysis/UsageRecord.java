package com.qianyi.resumeoptimizer.analysis;

import java.time.Instant;

public record UsageRecord(
        String resumeId,
        String model,
        int resumeChars,
        int jobDescriptionChars,
        int promptChars,
        int outputChars,
        String status,
        Instant createdAt
) {
}

