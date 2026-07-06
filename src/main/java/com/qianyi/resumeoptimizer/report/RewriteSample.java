package com.qianyi.resumeoptimizer.report;

import jakarta.validation.constraints.NotBlank;

public record RewriteSample(
        @NotBlank String beforeText,
        @NotBlank String afterText,
        @NotBlank String note
) {
}

