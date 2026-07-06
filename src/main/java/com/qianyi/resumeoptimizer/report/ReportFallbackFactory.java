package com.qianyi.resumeoptimizer.report;

import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class ReportFallbackFactory {

    public AnalysisReport modelOutputError(String position, String detail) {
        String safePosition = position == null || position.isBlank() ? "目标岗位" : position;
        String safeDetail = detail == null || detail.isBlank() ? "模型输出无法解析" : detail;

        // 回退报告仍遵守同一 JSON 协议，前端无需走第二套错误渲染逻辑。
        return new AnalysisReport(
                0,
                1,
                safePosition,
                "本次 AI 输出未通过结构化校验，请检查模型设置后重新生成。",
                List.of(new DimensionScore(
                        "MODEL_OUTPUT",
                        "模型输出",
                        0,
                        "模型返回内容缺少必填字段、分数越界或不是可解析的 JSON。"
                )),
                List.of(new Impression(
                        "需要重新生成",
                        "报告结构不完整，系统已保留错误信息，避免展示不可信结果。"
                )),
                List.of(new Suggestion(
                        SuggestionType.REWRITE,
                        "重新生成报告",
                        "当前模型输出没有满足稳定 JSON 协议。",
                        null,
                        "请确认 API Key、Base URL、模型名可用，并重新点击开始分析。",
                        "如果连续失败，建议换用更强模型或缩短简历/JD 输入。"
                )),
                List.of(),
                List.of(new RiskWarning(
                        RiskCategory.MISSING_DATA,
                        RiskSeverity.MEDIUM,
                        "没有获得可信的结构化分析结果。",
                        "不要直接使用本次报告，请重新生成后再复制建议。"
                )),
                ReportStatus.ERROR,
                safeDetail
        );
    }
}

