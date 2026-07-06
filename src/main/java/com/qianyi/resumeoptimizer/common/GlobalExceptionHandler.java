package com.qianyi.resumeoptimizer.common;

import com.qianyi.resumeoptimizer.resume.ResumeParseException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.multipart.MaxUploadSizeExceededException;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(ResumeParseException.class)
    public ResponseEntity<ApiError> handleResumeParse(ResumeParseException exception) {
        return ResponseEntity.badRequest().body(ApiError.of(exception.getMessage(), exception.code()));
    }

    @ExceptionHandler(MaxUploadSizeExceededException.class)
    public ResponseEntity<ApiError> handleUploadLimit() {
        return ResponseEntity.status(HttpStatus.PAYLOAD_TOO_LARGE)
                .body(ApiError.of("文件过大，请上传 8MB 以内的简历。", "FILE_TOO_LARGE"));
    }
}

