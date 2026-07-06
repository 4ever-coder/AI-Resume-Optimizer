package com.qianyi.resumeoptimizer;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.qianyi.resumeoptimizer.analysis.AnalysisProperties;
import com.qianyi.resumeoptimizer.analysis.AnalysisPromptBuilder;
import com.qianyi.resumeoptimizer.analysis.AnalysisRequest;
import com.qianyi.resumeoptimizer.analysis.AnalysisResponse;
import com.qianyi.resumeoptimizer.analysis.AnalysisService;
import com.qianyi.resumeoptimizer.analysis.ModelConnectionSettings;
import com.qianyi.resumeoptimizer.analysis.UsageLogService;
import com.qianyi.resumeoptimizer.report.ReportFallbackFactory;
import com.qianyi.resumeoptimizer.report.ReportValidationService;
import com.qianyi.resumeoptimizer.resume.ResumeDocument;
import com.qianyi.resumeoptimizer.resume.ResumeParserService;
import com.qianyi.resumeoptimizer.resume.ResumeProperties;
import com.qianyi.resumeoptimizer.resume.ResumeUploadService;
import com.qianyi.resumeoptimizer.resume.TextCleaner;
import jakarta.validation.Validation;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.springframework.mock.web.MockMultipartFile;

import java.nio.charset.StandardCharsets;
import java.nio.file.Path;

import static org.assertj.core.api.Assertions.assertThat;

class EndToEndWorkflowTests {

    @TempDir
    Path workspace;

    @Test
    void uploadResumeAndGenerateReportWithoutPersistingApiKey() throws Exception {
        ObjectMapper objectMapper = new ObjectMapper().registerModule(new JavaTimeModule());
        ResumeProperties resumeProperties = new ResumeProperties(8 * 1024 * 1024, 20, 20);
        ResumeUploadService uploadService = new ResumeUploadService(
                workspace.resolve("uploads"),
                resumeProperties,
                new ResumeParserService(resumeProperties, new TextCleaner()),
                objectMapper
        );

        AnalysisProperties analysisProperties = new AnalysisProperties(
                "https://api.openai.com/v1",
                "gpt-4.1-mini",
                30000,
                12000,
                workspace.resolve("usage.jsonl").toString()
        );
        AnalysisService analysisService = new AnalysisService(
                uploadService,
                new AnalysisPromptBuilder(),
                (settings, prompt) -> """
                        {
                          "summaryScore": 91,
                          "starLevel": 5,
                          "position": "Java 后端工程师",
                          "overallComment": "整体匹配度高，建议强化业务指标。",
                          "dimensions": [{"key":"POSITION_MATCH","label":"岗位匹配度","score":90,"comment":"核心技能匹配。"}],
                          "impressions": [{"label":"匹配度高","description":"项目经验与岗位方向一致。"}],
                          "suggestions": [{"type":"SUPPLEMENT","title":"补充业务指标","reason":"简历缺少规模数据","originalText":"负责服务开发","improvedText":"负责核心服务开发，支撑日均万级请求","actionHint":"补充请求量、响应时间或转化数据"}],
                          "rewriteSamples": [{"beforeText":"负责服务开发","afterText":"负责核心服务开发，支撑日均万级请求","note":"加入业务规模"}],
                          "riskWarnings": [],
                          "status": "OK",
                          "errorMessage": null
                        }
                        """,
                objectMapper,
                new ReportValidationService(Validation.buildDefaultValidatorFactory().getValidator()),
                new ReportFallbackFactory(),
                analysisProperties,
                new UsageLogService(analysisProperties, objectMapper)
        );

        ResumeDocument resume = uploadService.upload(new MockMultipartFile(
                "file",
                "resume.txt",
                "text/plain",
                "项目经历\n负责 Java Spring Boot 核心服务开发，优化接口性能并沉淀规范".getBytes(StandardCharsets.UTF_8)
        ));
        AnalysisResponse response = analysisService.analyze(new AnalysisRequest(
                resume.id(),
                "Java 后端工程师",
                "要求熟悉 Spring Boot、接口性能优化和 MySQL",
                new ModelConnectionSettings("sk-should-not-be-logged", "https://api.example.com/v1", "gpt-test")
        ));

        assertThat(response.report().summaryScore()).isEqualTo(91);
        assertThat(response.report().suggestions()).hasSize(1);
        assertThat(java.nio.file.Files.readString(workspace.resolve("usage.jsonl")))
                .contains("SUCCESS")
                .doesNotContain("sk-should-not-be-logged");
    }
}

