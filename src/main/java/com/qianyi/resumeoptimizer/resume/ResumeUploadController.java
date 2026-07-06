package com.qianyi.resumeoptimizer.resume;

import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

@RestController
@RequestMapping("/api/resumes")
public class ResumeUploadController {

    private final ResumeUploadService resumeUploadService;

    public ResumeUploadController(ResumeUploadService resumeUploadService) {
        this.resumeUploadService = resumeUploadService;
    }

    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResumeDocument upload(@RequestPart("file") MultipartFile file) throws IOException {
        return resumeUploadService.upload(file);
    }
}

