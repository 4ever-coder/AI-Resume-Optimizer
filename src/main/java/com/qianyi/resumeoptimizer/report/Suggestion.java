package com.qianyi.resumeoptimizer.report;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record Suggestion(
        @NotNull SuggestionType type,
        @NotBlank String title,
        @NotBlank String reason,
        String originalText,
        @NotBlank String improvedText,
        @NotBlank String actionHint
) {
}

