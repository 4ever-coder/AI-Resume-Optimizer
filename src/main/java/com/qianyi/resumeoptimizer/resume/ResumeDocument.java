package com.qianyi.resumeoptimizer.resume;

import java.time.Instant;

public record ResumeDocument(
        String id,
        String originalFilename,
        String storedFilename,
        String contentType,
        String extension,
        long sizeBytes,
        int pageCount,
        int textLength,
        String parsedText,
        Instant uploadedAt
) {
}

