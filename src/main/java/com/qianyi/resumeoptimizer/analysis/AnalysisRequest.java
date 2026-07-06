package com.qianyi.resumeoptimizer.analysis;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record AnalysisRequest(
        @NotBlank String resumeId,
        @NotBlank String position,
        String jobDescription,
        @Valid @NotNull ModelConnectionSettings modelSettings
) {
}

