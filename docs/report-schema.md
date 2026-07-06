# 报告 JSON 协议

`QIA-123` 定义后端可校验、前端可直接渲染的稳定报告结构。模型输出必须能映射为 `AnalysisReport` DTO，并满足 `src/main/resources/schemas/report.schema.json`。

## 顶层字段

- `summaryScore`：总分，整数，范围 0-100。
- `starLevel`：星级，整数，范围 1-5。
- `position`：目标岗位或岗位方向，必填。
- `overallComment`：整体评价，必填。
- `dimensions`：维度评分列表，至少 1 项。
- `impressions`：整体印象列表，至少 1 项。
- `suggestions`：优化建议卡片，至少 1 项。
- `rewriteSamples`：改写样例列表，可为空数组。
- `riskWarnings`：风险提示列表，可为空数组。
- `status`：报告状态，`OK` 或 `ERROR`。
- `errorMessage`：异常回退时给用户看的可读错误。

## 维度建议

- `POSITION_MATCH`：岗位匹配度，关注岗位关键词、核心技能、年限/项目深度、业务场景。
- `CAPABILITY_DEPTH`：技术/能力深度，关注技能栈掌握、项目复杂度、量化成果。
- `EXPRESSION_QUALITY`：表达质量，关注 STAR 表述、动词开头、冗余程度、可读性。
- `RISK_CONTROL`：风险提示，关注真实性、空窗/跳槽、缺少数据、岗位不匹配。

## 异常回退

当模型返回非 JSON、字段缺失、分数越界或枚举不合法时，后端使用 `ReportFallbackFactory` 生成可读的 `ERROR` 报告。前端仍然可以按同一 JSON 协议渲染错误说明，避免空白页。

