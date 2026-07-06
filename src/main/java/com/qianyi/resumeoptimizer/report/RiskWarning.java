package com.qianyi.resumeoptimizer.report;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record RiskWarning(
        @NotNull RiskCategory category,
        @NotNull RiskSeverity severity,
        @NotBlank String message,
        @NotBlank String action
) {
}

