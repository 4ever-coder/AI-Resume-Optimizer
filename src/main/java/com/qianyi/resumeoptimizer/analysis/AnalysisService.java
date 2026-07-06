package com.qianyi.resumeoptimizer.analysis;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.qianyi.resumeoptimizer.report.AnalysisReport;
import com.qianyi.resumeoptimizer.report.ReportFallbackFactory;
import com.qianyi.resumeoptimizer.report.ReportValidationException;
import com.qianyi.resumeoptimizer.report.ReportValidationService;
import com.qianyi.resumeoptimizer.resume.ResumeDocument;
import com.qianyi.resumeoptimizer.resume.ResumeUploadService;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.time.Instant;

@Service
public class AnalysisService {

    private final ResumeUploadService resumeUploadService;
    private final AnalysisPromptBuilder promptBuilder;
    private final ChatModelClient chatModelClient;
    private final ObjectMapper objectMapper;
    private final ReportValidationService validationService;
    private final ReportFallbackFactory fallbackFactory;

    public AnalysisService(
            ResumeUploadService resumeUploadService,
            AnalysisPromptBuilder promptBuilder,
            ChatModelClient chatModelClient,
            ObjectMapper objectMapper,
            ReportValidationService validationService,
            ReportFallbackFactory fallbackFactory
    ) {
        this.resumeUploadService = resumeUploadService;
        this.promptBuilder = promptBuilder;
        this.chatModelClient = chatModelClient;
        this.objectMapper = objectMapper;
        this.validationService = validationService;
        this.fallbackFactory = fallbackFactory;
    }

    public AnalysisResponse analyze(AnalysisRequest request) {
        validateModelSettings(request.modelSettings());
        try {
            ResumeDocument resume = resumeUploadService.getById(request.resumeId());
            AnalysisPrompt prompt = promptBuilder.build(resume.parsedText(), request.position(), request.jobDescription());
            String modelOutput = chatModelClient.complete(request.modelSettings(), prompt);
            AnalysisReport report = parseAndValidate(modelOutput, request.position());
            return new AnalysisResponse(resume.id(), request.position(), request.modelSettings().model(), report, Instant.now());
        } catch (IOException exception) {
            throw new AnalysisException("读取简历历史记录失败，请重新上传简历。", "RESUME_READ_FAILED");
        }
    }

    private AnalysisReport parseAndValidate(String modelOutput, String position) {
        try {
            AnalysisReport report = objectMapper.readValue(stripJsonFence(modelOutput), AnalysisReport.class);
            return validationService.requireValid(report);
        } catch (JsonProcessingException | ReportValidationException exception) {
            return fallbackFactory.modelOutputError(position, exception.getMessage());
        }
    }

    private void validateModelSettings(ModelConnectionSettings settings) {
        if (settings.apiKey() == null || settings.apiKey().isBlank()) {
            throw new AnalysisException("请先在模型设置中填写 API Key。", "MISSING_API_KEY");
        }
        if (settings.baseUrl() == null || settings.baseUrl().isBlank()) {
            throw new AnalysisException("请填写模型 Base URL。", "MISSING_BASE_URL");
        }
        if (settings.model() == null || settings.model().isBlank()) {
            throw new AnalysisException("请填写模型名称。", "MISSING_MODEL");
        }
    }

    private String stripJsonFence(String content) {
        String text = content == null ? "" : content.trim();
        if (text.startsWith("```")) {
            text = text.replaceFirst("^```(?:json)?", "").replaceFirst("```$", "").trim();
        }
        return text;
    }
}

