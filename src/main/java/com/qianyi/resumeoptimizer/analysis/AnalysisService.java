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
    private final AnalysisProperties properties;
    private final UsageLogService usageLogService;

    public AnalysisService(
            ResumeUploadService resumeUploadService,
            AnalysisPromptBuilder promptBuilder,
            ChatModelClient chatModelClient,
            ObjectMapper objectMapper,
            ReportValidationService validationService,
            ReportFallbackFactory fallbackFactory,
            AnalysisProperties properties,
            UsageLogService usageLogService
    ) {
        this.resumeUploadService = resumeUploadService;
        this.promptBuilder = promptBuilder;
        this.chatModelClient = chatModelClient;
        this.objectMapper = objectMapper;
        this.validationService = validationService;
        this.fallbackFactory = fallbackFactory;
        this.properties = properties;
        this.usageLogService = usageLogService;
    }

    public AnalysisResponse analyze(AnalysisRequest request) {
        validateModelSettings(request.modelSettings());
        try {
            ResumeDocument resume = resumeUploadService.getById(request.resumeId());
            String resumeText = limitText(resume.parsedText(), properties.maxResumeChars(), "简历文本过长，已截断到限制范围。");
            String jobDescription = limitText(request.jobDescription(), properties.maxJobDescriptionChars(), "岗位 JD 过长，已截断到限制范围。");
            AnalysisPrompt prompt = promptBuilder.build(resumeText, request.position(), jobDescription);
            String modelOutput;
            try {
                modelOutput = chatModelClient.complete(request.modelSettings(), prompt);
            } catch (AnalysisException exception) {
                usageLogService.record(usageLogService.failed(resume.id(), request.modelSettings().model(), resumeText.length(), jobDescription.length(), prompt.systemPrompt().length() + prompt.userPrompt().length()));
                throw exception;
            }
            AnalysisReport report = parseAndValidate(modelOutput, request.position());
            usageLogService.record(usageLogService.success(
                    resume.id(),
                    request.modelSettings().model(),
                    resumeText.length(),
                    jobDescription.length(),
                    prompt.systemPrompt().length() + prompt.userPrompt().length(),
                    modelOutput.length()
            ));
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
        String baseUrl = settings.baseUrl().trim();
        if (!baseUrl.startsWith("https://") && !baseUrl.startsWith("http://")) {
            throw new AnalysisException("Base URL 必须以 http:// 或 https:// 开头。", "INVALID_BASE_URL");
        }
    }

    private String stripJsonFence(String content) {
        String text = content == null ? "" : content.trim();
        if (text.startsWith("```")) {
            text = text.replaceFirst("^```(?:json)?", "").replaceFirst("```$", "").trim();
        }
        return text;
    }

    private String limitText(String text, int maxChars, String message) {
        if (text == null) {
            return "";
        }
        if (text.length() <= maxChars) {
            return text;
        }
        return text.substring(0, maxChars) + "\n\n" + message;
    }
}
