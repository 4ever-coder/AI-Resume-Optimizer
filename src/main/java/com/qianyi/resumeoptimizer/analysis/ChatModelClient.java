package com.qianyi.resumeoptimizer.analysis;

public interface ChatModelClient {

    String complete(ModelConnectionSettings settings, AnalysisPrompt prompt);
}

