package com.qianyi.resumeoptimizer.analysis;

import java.util.List;

public final class AnalysisRules {

    public static final List<String> DIMENSIONS = List.of(
            "POSITION_MATCH",
            "CAPABILITY_DEPTH",
            "EXPRESSION_QUALITY",
            "RISK_CONTROL"
    );

    public static final List<String> REQUIRED_TOP_LEVEL_FIELDS = List.of(
            "summaryScore",
            "starLevel",
            "position",
            "overallComment",
            "dimensions",
            "impressions",
            "suggestions",
            "rewriteSamples",
            "riskWarnings",
            "status",
            "errorMessage"
    );

    private AnalysisRules() {
    }
}

