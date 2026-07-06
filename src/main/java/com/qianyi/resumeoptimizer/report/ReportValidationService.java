package com.qianyi.resumeoptimizer.report;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validator;
import org.springframework.stereotype.Service;

import java.util.Comparator;
import java.util.List;

@Service
public class ReportValidationService {

    private final Validator validator;

    public ReportValidationService(Validator validator) {
        this.validator = validator;
    }

    public List<String> validate(AnalysisReport report) {
        return validator.validate(report).stream()
                .sorted(Comparator.comparing(violation -> violation.getPropertyPath().toString()))
                .map(this::formatViolation)
                .toList();
    }

    public AnalysisReport requireValid(AnalysisReport report) {
        List<String> errors = validate(report);
        if (!errors.isEmpty()) {
            throw new ReportValidationException(errors);
        }
        return report;
    }

    private String formatViolation(ConstraintViolation<AnalysisReport> violation) {
        return violation.getPropertyPath() + " " + violation.getMessage();
    }
}

