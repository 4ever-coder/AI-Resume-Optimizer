package com.qianyi.resumeoptimizer.analysis;

import com.qianyi.resumeoptimizer.report.AnalysisReport;

import java.time.Instant;

public record AnalysisResponse(
        String resumeId,
        String position,
        String model,
        AnalysisReport report,
        Instant createdAt
) {
}

