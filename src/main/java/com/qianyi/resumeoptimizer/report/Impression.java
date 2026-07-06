package com.qianyi.resumeoptimizer.report;

import jakarta.validation.constraints.NotBlank;

public record Impression(
        @NotBlank String label,
        @NotBlank String description
) {
}

