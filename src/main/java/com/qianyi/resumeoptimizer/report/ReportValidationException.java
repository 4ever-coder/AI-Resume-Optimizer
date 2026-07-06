package com.qianyi.resumeoptimizer.report;

import java.util.List;

public class ReportValidationException extends RuntimeException {

    private final List<String> errors;

    public ReportValidationException(List<String> errors) {
        super(String.join("; ", errors));
        this.errors = List.copyOf(errors);
    }

    public List<String> errors() {
        return errors;
    }
}

