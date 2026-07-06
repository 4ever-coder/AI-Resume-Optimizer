package com.qianyi.resumeoptimizer.resume;

public class ResumeParseException extends RuntimeException {

    private final String code;

    public ResumeParseException(String message, String code) {
        super(message);
        this.code = code;
    }

    public String code() {
        return code;
    }
}

