package com.qianyi.resumeoptimizer.analysis;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.qianyi.resumeoptimizer.report.ReportFallbackFactory;
import com.qianyi.resumeoptimizer.report.ReportValidationService;
import com.qianyi.resumeoptimizer.resume.ResumeProperties;
import com.qianyi.resumeoptimizer.resume.ResumeParserService;
import com.qianyi.resumeoptimizer.resume.ResumeUploadService;
import com.qianyi.resumeoptimizer.resume.TextCleaner;
import jakarta.validation.Validation;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.springframework.mock.web.MockMultipartFile;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class AnalysisServiceTests {

    @TempDir
    Path uploadDir;

    @Test
    void analyzesResumeWithValidModelJson() throws Exception {
        AnalysisService service = service("""
                {
                  "summaryScore": 88,
                  "starLevel": 4,
                  "position": "Java 后端工程师",
                  "overallComment": "项目经历扎实，建议补充量化结果。",
                  "dimensions": [{"key":"POSITION_MATCH","label":"岗位匹配度","score":86,"comment":"技能匹配。"}],
                  "impressions": [{"label":"基础扎实","description":"后端项目经历完整。"}],
                  "suggestions": [{"type":"EXPAND","title":"补充指标","reason":"缺少量化成果","originalText":"负责接口开发","improvedText":"负责接口开发并优化响应时间","actionHint":"补充响应时间和业务规模"}],
                  "rewriteSamples": [],
                  "riskWarnings": [],
                  "status": "OK",
                  "errorMessage": null
                }
                """);
        String resumeId = uploadResume();

        AnalysisResponse response = service.analyze(new AnalysisRequest(
                resumeId,
                "Java 后端工程师",
                "熟悉 Spring Boot",
                new ModelConnectionSettings("sk-test", "https://api.example.com/v1", "gpt-test")
        ));

        assertThat(response.report().summaryScore()).isEqualTo(88);
        assertThat(response.model()).isEqualTo("gpt-test");
    }

    @Test
    void returnsFallbackReportWhenModelJsonIsInvalid() throws Exception {
        AnalysisService service = service("not-json");
        String resumeId = uploadResume();

        AnalysisResponse response = service.analyze(new AnalysisRequest(
                resumeId,
                "Java 后端工程师",
                "",
                new ModelConnectionSettings("sk-test", "https://api.example.com/v1", "gpt-test")
        ));

        assertThat(response.report().status().name()).isEqualTo("ERROR");
        assertThat(response.report().errorMessage()).isNotBlank();
    }

    @Test
    void rejectsMissingApiKeyBeforeCallingModel() {
        assertThatThrownBy(() -> service("{}").analyze(new AnalysisRequest(
                "resume-id",
                "Java 后端工程师",
                "",
                new ModelConnectionSettings("", "https://api.example.com/v1", "gpt-test")
        ))).isInstanceOf(AnalysisException.class)
                .hasMessageContaining("API Key");
    }

    private AnalysisService service(String modelOutput) {
        ObjectMapper objectMapper = new ObjectMapper().registerModule(new JavaTimeModule());
        ResumeUploadService uploadService = uploadService(objectMapper);
        return new AnalysisService(
                uploadService,
                new AnalysisPromptBuilder(),
                (settings, prompt) -> modelOutput,
                objectMapper,
                new ReportValidationService(Validation.buildDefaultValidatorFactory().getValidator()),
                new ReportFallbackFactory(),
                new AnalysisProperties("https://api.openai.com/v1", "gpt-4.1-mini", 30000, 12000, uploadDir.resolve("usage.jsonl").toString()),
                new UsageLogService(new AnalysisProperties("https://api.openai.com/v1", "gpt-4.1-mini", 30000, 12000, uploadDir.resolve("usage.jsonl").toString()), objectMapper)
        );
    }

    private ResumeUploadService uploadService(ObjectMapper objectMapper) {
        ResumeProperties properties = new ResumeProperties(8 * 1024 * 1024, 20, 20);
        return new ResumeUploadService(
                uploadDir,
                properties,
                new ResumeParserService(properties, new TextCleaner()),
                objectMapper
        );
    }

    private String uploadResume() throws Exception {
        ResumeUploadService uploadService = uploadService(new ObjectMapper().registerModule(new JavaTimeModule()));
        return uploadService.upload(new MockMultipartFile(
                "file",
                "resume.txt",
                "text/plain",
                "项目经历\n负责 Java Spring Boot 服务开发并优化接口性能".getBytes(StandardCharsets.UTF_8)
        )).id();
    }

    @Test
    void rejectsInvalidBaseUrlProtocol() {
        assertThatThrownBy(() -> service("{}").analyze(new AnalysisRequest(
                "resume-id",
                "Java 后端工程师",
                "",
                new ModelConnectionSettings("sk-test", "file:///tmp/model", "gpt-test")
        ))).isInstanceOf(AnalysisException.class)
                .hasMessageContaining("Base URL");
    }

    @Test
    void recordsUsageWithoutApiKey() throws Exception {
        AnalysisService service = service("""
                {
                  "summaryScore": 88,
                  "starLevel": 4,
                  "position": "Java 后端工程师",
                  "overallComment": "项目经历扎实。",
                  "dimensions": [{"key":"POSITION_MATCH","label":"岗位匹配度","score":86,"comment":"技能匹配。"}],
                  "impressions": [{"label":"基础扎实","description":"后端项目经历完整。"}],
                  "suggestions": [{"type":"EXPAND","title":"补充指标","reason":"缺少量化成果","originalText":"负责接口开发","improvedText":"负责接口开发并优化响应时间","actionHint":"补充响应时间"}],
                  "rewriteSamples": [],
                  "riskWarnings": [],
                  "status": "OK",
                  "errorMessage": null
                }
                """);
        String resumeId = uploadResume();

        service.analyze(new AnalysisRequest(
                resumeId,
                "Java 后端工程师",
                "熟悉 Spring Boot",
                new ModelConnectionSettings("sk-secret", "https://api.example.com/v1", "gpt-test")
        ));

        String usage = Files.readString(uploadDir.resolve("usage.jsonl"));
        assertThat(usage).contains("gpt-test", "SUCCESS").doesNotContain("sk-secret");
    }
}
