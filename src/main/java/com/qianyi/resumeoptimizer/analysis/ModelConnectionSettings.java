package com.qianyi.resumeoptimizer.analysis;

import jakarta.validation.constraints.NotBlank;

public record ModelConnectionSettings(
        @NotBlank String apiKey,
        @NotBlank String baseUrl,
        @NotBlank String model
) {
}

