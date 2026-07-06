package com.qianyi.resumeoptimizer.analysis;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class AnalysisPromptBuilderTests {

    private final AnalysisPromptBuilder builder = new AnalysisPromptBuilder();

    @Test
    void promptIncludesStableOutputContract() {
        AnalysisPrompt prompt = builder.build("项目经历\n负责订单接口优化", "Java 后端工程师", "熟悉 Spring Boot");

        assertThat(prompt.systemPrompt())
                .contains("只输出 JSON")
                .contains("summaryScore")
                .contains("suggestions")
                .contains("TRIM、EXPAND、SUPPLEMENT、REWRITE、AI_FUSION");
        assertThat(prompt.userPrompt())
                .contains("Java 后端工程师")
                .contains("熟悉 Spring Boot")
                .contains("负责订单接口优化");
    }

    @Test
    void rulesExposeRequiredFieldsForValidationAndDocs() {
        assertThat(AnalysisRules.REQUIRED_TOP_LEVEL_FIELDS)
                .contains("summaryScore", "starLevel", "riskWarnings", "errorMessage");
        assertThat(AnalysisRules.DIMENSIONS)
                .containsExactly("POSITION_MATCH", "CAPABILITY_DEPTH", "EXPRESSION_QUALITY", "RISK_CONTROL");
    }
}

