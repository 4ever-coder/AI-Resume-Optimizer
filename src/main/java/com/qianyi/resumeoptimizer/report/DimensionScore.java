package com.qianyi.resumeoptimizer.report;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;

public record DimensionScore(
        @NotBlank String key,
        @NotBlank String label,
        @Min(0) @Max(100) int score,
        @NotBlank String comment
) {
}

