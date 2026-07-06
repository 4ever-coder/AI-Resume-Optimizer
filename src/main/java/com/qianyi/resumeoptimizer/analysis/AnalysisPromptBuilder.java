package com.qianyi.resumeoptimizer.analysis;

import org.springframework.stereotype.Component;

@Component
public class AnalysisPromptBuilder {

    public AnalysisPrompt build(String resumeText, String position, String jobDescription) {
        String safePosition = blankToDefault(position, "目标岗位");
        String safeJobDescription = blankToDefault(jobDescription, "用户未提供 JD，请按岗位名称和简历内容分析。");

        return new AnalysisPrompt(systemPrompt(), """
                目标岗位：
                %s

                目标 JD：
                %s

                简历文本：
                %s
                """.formatted(safePosition, safeJobDescription, resumeText));
    }

    private String systemPrompt() {
        return """
                你是资深招聘顾问和简历优化专家。请严格按照以下规则分析简历：

                评分维度：
                1. 岗位匹配度：岗位关键词、核心技能、年限/项目深度、业务场景。
                2. 技术/能力深度：技能栈掌握、项目复杂度、量化成果。
                3. 表达质量：STAR 表述、动词开头、冗余程度、可读性。
                4. 风险提示：经验真实性、空窗/跳槽、缺少数据、岗位不匹配。

                建议类型枚举只能使用：TRIM、EXPAND、SUPPLEMENT、REWRITE、AI_FUSION。
                风险类别枚举只能使用：AUTHENTICITY、GAP、MISSING_DATA、POSITION_MISMATCH。
                风险等级枚举只能使用：LOW、MEDIUM、HIGH。

                只输出 JSON，不输出 Markdown、解释或代码块。JSON 顶层必须包含：
                summaryScore、starLevel、position、overallComment、dimensions、impressions、
                suggestions、rewriteSamples、riskWarnings、status、errorMessage。

                正常报告 status 必须为 OK，errorMessage 必须为 null。
                summaryScore 和 dimensions[].score 必须是 0 到 100 的整数。
                starLevel 必须是 1 到 5 的整数，并与 summaryScore 大致匹配。
                suggestions 必须给出可直接执行的优化动作，不要只写泛泛建议。
                """;
    }

    private String blankToDefault(String value, String fallback) {
        return value == null || value.isBlank() ? fallback : value.trim();
    }
}

