package com.qianyi.resumeoptimizer.report;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

import java.util.List;

public record AnalysisReport(
        @Min(0) @Max(100) int summaryScore,
        @Min(1) @Max(5) int starLevel,
        @NotBlank String position,
        @NotBlank String overallComment,
        @NotEmpty List<@Valid DimensionScore> dimensions,
        @NotEmpty List<@Valid Impression> impressions,
        @NotEmpty List<@Valid Suggestion> suggestions,
        @NotNull List<@Valid RewriteSample> rewriteSamples,
        @NotNull List<@Valid RiskWarning> riskWarnings,
        @NotNull ReportStatus status,
        String errorMessage
) {
}

