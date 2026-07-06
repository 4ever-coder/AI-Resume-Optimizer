# Analysis Rules

本规则用于约束 AI 简历分析输出，保证模型返回可以被 `AnalysisReport` DTO 和 JSON Schema 校验。

## 输入

- 简历解析文本：来自 `/api/resumes` 上传解析结果。
- 目标岗位：用户填写的岗位名称。
- 目标 JD：用户可选粘贴的岗位描述。

## 评分口径

- 总分 `summaryScore`：0-100，综合以下维度。
- 星级 `starLevel`：1-5，根据总分映射：90+ 为 5 星，80-89 为 4 星，65-79 为 3 星，50-64 为 2 星，低于 50 为 1 星。
- 岗位匹配度：岗位关键词、核心技能、年限/项目深度、业务场景。
- 技术/能力深度：技能栈掌握、项目复杂度、量化成果。
- 表达质量：STAR 表述、动词开头、冗余程度、可读性。
- 风险提示：经验真实性、空窗/跳槽、缺少数据、岗位不匹配。

## 建议类型

- `TRIM`：精简冗余。
- `EXPAND`：扩展项目细节和职责。
- `SUPPLEMENT`：补充缺失关键词、指标或业务背景。
- `REWRITE`：改写低质量表达。
- `AI_FUSION`：把用户经历与目标 JD 关键词融合。

## 输出要求

- 只输出 JSON，不输出 Markdown。
- 顶层字段必须完整：`summaryScore`、`starLevel`、`position`、`overallComment`、`dimensions`、`impressions`、`suggestions`、`rewriteSamples`、`riskWarnings`、`status`、`errorMessage`。
- 正常报告 `status` 必须为 `OK`，`errorMessage` 为 `null`。
- 所有分数必须是整数，且在 0-100 范围内。
- 建议必须可执行，避免空泛措辞。

