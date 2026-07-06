package com.qianyi.resumeoptimizer.report;

import jakarta.validation.Validation;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;

class ReportValidationServiceTests {

    private final ReportValidationService service =
            new ReportValidationService(Validation.buildDefaultValidatorFactory().getValidator());

    @Test
    void acceptsStableReportContract() {
        assertThat(service.validate(validReport())).isEmpty();
    }

    @Test
    void rejectsOutOfRangeScoreAndMissingRequiredFields() {
        AnalysisReport invalid = new AnalysisReport(
                120,
                6,
                "",
                "",
                List.of(),
                List.of(),
                List.of(),
                List.of(),
                List.of(),
                ReportStatus.OK,
                null
        );

        assertThat(service.validate(invalid))
                .anyMatch(error -> error.contains("summaryScore"))
                .anyMatch(error -> error.contains("starLevel"))
                .anyMatch(error -> error.contains("position"))
                .anyMatch(error -> error.contains("suggestions"));
    }

    @Test
    void throwsReadableExceptionWhenReportIsInvalid() {
        assertThatCode(() -> service.requireValid(new ReportFallbackFactory().modelOutputError("", "")))
                .doesNotThrowAnyException();
    }

    @Test
    void fallbackReportUsesTheSameRenderableContract() {
        AnalysisReport fallback = new ReportFallbackFactory()
                .modelOutputError("Java 后端工程师", "模型返回了非 JSON 内容");

        assertThat(service.validate(fallback)).isEmpty();
        assertThat(fallback.status()).isEqualTo(ReportStatus.ERROR);
        assertThat(fallback.errorMessage()).contains("非 JSON");
    }

    private AnalysisReport validReport() {
        return new AnalysisReport(
                86,
                4,
                "Java 后端工程师",
                "项目经历完整，但量化成果和岗位关键词还可以继续加强。",
                List.of(new DimensionScore("POSITION_MATCH", "岗位匹配度", 82, "技能栈与岗位要求基本一致。")),
                List.of(new Impression("项目完整", "有清晰的项目背景、职责和技术栈。")),
                List.of(new Suggestion(
                        SuggestionType.EXPAND,
                        "补充量化成果",
                        "当前项目描述缺少可验证的数据。",
                        "负责接口开发",
                        "负责订单接口开发，将核心查询耗时从 800ms 优化到 230ms。",
                        "在项目经历中补充指标、规模和结果。"
                )),
                List.of(new RewriteSample("负责接口开发", "负责订单接口开发，将核心查询耗时从 800ms 优化到 230ms。", "加入动作和结果。")),
                List.of(new RiskWarning(RiskCategory.MISSING_DATA, RiskSeverity.LOW, "缺少项目规模数据。", "补充用户量、QPS 或数据量。")),
                ReportStatus.OK,
                null
        );
    }
}
