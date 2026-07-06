package com.qianyi.resumeoptimizer.resume;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Instant;
import java.util.Locale;
import java.util.Set;
import java.util.UUID;

@Service
public class ResumeUploadService {

    private static final Set<String> SUPPORTED_EXTENSIONS = Set.of("pdf", "docx", "txt");

    private final Path uploadDir;
    private final ResumeProperties properties;
    private final ResumeParserService parserService;
    private final ObjectMapper objectMapper;

    public ResumeUploadService(
            @Value("${app.storage.upload-dir}") Path uploadDir,
            ResumeProperties properties,
            ResumeParserService parserService,
            ObjectMapper objectMapper
    ) {
        this.uploadDir = uploadDir;
        this.properties = properties;
        this.parserService = parserService;
        this.objectMapper = objectMapper;
    }

    public ResumeDocument upload(MultipartFile file) throws IOException {
        validateFile(file);

        String extension = extensionOf(file.getOriginalFilename());
        byte[] content = file.getBytes();
        ParsedResume parsedResume = parserService.parse(content, extension);
        String id = UUID.randomUUID().toString();
        String storedFilename = id + "." + extension;

        Files.createDirectories(uploadDir);
        Files.write(uploadDir.resolve(storedFilename), content);

        ResumeDocument document = new ResumeDocument(
                id,
                safeOriginalFilename(file.getOriginalFilename()),
                storedFilename,
                file.getContentType(),
                extension,
                file.getSize(),
                parsedResume.pageCount(),
                parsedResume.text().length(),
                parsedResume.text(),
                Instant.now()
        );

        // 元信息和解析文本单独保存，后续历史记录和分析接口可以直接复用。
        objectMapper.writerWithDefaultPrettyPrinter()
                .writeValue(uploadDir.resolve(id + ".json").toFile(), document);
        return document;
    }

    private void validateFile(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new ResumeParseException("请选择要上传的简历文件。", "EMPTY_FILE");
        }
        if (file.getSize() > properties.maxFileSizeBytes()) {
            throw new ResumeParseException("文件过大，请上传 8MB 以内的简历。", "FILE_TOO_LARGE");
        }
        String extension = extensionOf(file.getOriginalFilename());
        if (!SUPPORTED_EXTENSIONS.contains(extension)) {
            throw new ResumeParseException("仅支持 PDF、DOCX、TXT 简历文件。", "UNSUPPORTED_FILE_TYPE");
        }
    }

    private String extensionOf(String filename) {
        if (filename == null || !filename.contains(".")) {
            return "";
        }
        return filename.substring(filename.lastIndexOf('.') + 1).toLowerCase(Locale.ROOT);
    }

    private String safeOriginalFilename(String filename) {
        return filename == null || filename.isBlank() ? "unknown" : Path.of(filename).getFileName().toString();
    }
}

