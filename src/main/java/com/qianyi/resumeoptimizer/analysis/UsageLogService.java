package com.qianyi.resumeoptimizer.analysis;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Instant;

@Service
public class UsageLogService {

    private final Path usageLogFile;
    private final ObjectMapper objectMapper;

    public UsageLogService(AnalysisProperties properties, ObjectMapper objectMapper) {
        this.usageLogFile = Path.of(properties.usageLogFile());
        this.objectMapper = objectMapper;
    }

    public void record(UsageRecord record) {
        try {
            Files.createDirectories(usageLogFile.getParent());
            Files.writeString(
                    usageLogFile,
                    toJson(record) + System.lineSeparator(),
                    StandardCharsets.UTF_8,
                    Files.exists(usageLogFile)
                            ? java.nio.file.StandardOpenOption.APPEND
                            : java.nio.file.StandardOpenOption.CREATE
            );
        } catch (IOException exception) {
            // 用量日志不能影响用户主流程，后续可接入正式日志系统报警。
        }
    }

    private String toJson(UsageRecord record) throws JsonProcessingException {
        return objectMapper.writeValueAsString(record);
    }

    public UsageRecord success(String resumeId, String model, int resumeChars, int jdChars, int promptChars, int outputChars) {
        return new UsageRecord(resumeId, model, resumeChars, jdChars, promptChars, outputChars, "SUCCESS", Instant.now());
    }

    public UsageRecord failed(String resumeId, String model, int resumeChars, int jdChars, int promptChars) {
        return new UsageRecord(resumeId, model, resumeChars, jdChars, promptChars, 0, "FAILED", Instant.now());
    }
}

