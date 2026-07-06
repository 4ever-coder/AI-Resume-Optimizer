package com.qianyi.resumeoptimizer.resume;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.springframework.mock.web.MockMultipartFile;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class ResumeUploadServiceTests {

    @TempDir
    Path uploadDir;

    @Test
    void storesFileWithUuidNameAndMetadata() throws Exception {
        ResumeUploadService service = service();
        MockMultipartFile file = new MockMultipartFile(
                "file",
                "我的简历.txt",
                "text/plain",
                "项目经历\n负责 Java Spring Boot 服务开发并优化查询性能".getBytes(StandardCharsets.UTF_8)
        );

        ResumeDocument document = service.upload(file);

        assertThat(document.originalFilename()).isEqualTo("我的简历.txt");
        assertThat(document.storedFilename()).endsWith(".txt").doesNotContain("我的简历");
        assertThat(document.parsedText()).contains("项目经历", "Spring Boot");
        assertThat(Files.exists(uploadDir.resolve(document.storedFilename()))).isTrue();
        assertThat(Files.exists(uploadDir.resolve(document.id() + ".json"))).isTrue();
    }

    @Test
    void rejectsUnsupportedFileType() {
        MockMultipartFile file = new MockMultipartFile(
                "file",
                "resume.png",
                "image/png",
                "not a resume".getBytes(StandardCharsets.UTF_8)
        );

        assertThatThrownBy(() -> service().upload(file))
                .isInstanceOf(ResumeParseException.class)
                .hasMessageContaining("仅支持");
    }

    @Test
    void listsHistoryAndLoadsDetail() throws Exception {
        ResumeUploadService service = service();
        ResumeDocument first = service.upload(new MockMultipartFile(
                "file",
                "first.txt",
                "text/plain",
                "项目经历\n负责 Java 服务开发并沉淀接口规范".getBytes(StandardCharsets.UTF_8)
        ));
        ResumeDocument second = service.upload(new MockMultipartFile(
                "file",
                "second.txt",
                "text/plain",
                "教育经历\n计算机科学与技术 本科 参与多个课程项目".getBytes(StandardCharsets.UTF_8)
        ));

        assertThat(service.listHistory())
                .extracting(ResumeDocument::id)
                .contains(second.id(), first.id());
        assertThat(service.getById(first.id()).parsedText()).contains("接口规范");
    }

    private ResumeUploadService service() {
        ResumeProperties properties = new ResumeProperties(8 * 1024 * 1024, 20, 20);
        ResumeParserService parserService = new ResumeParserService(properties, new TextCleaner());
        return new ResumeUploadService(uploadDir, properties, parserService, new ObjectMapper().registerModule(new JavaTimeModule()));
    }
}
