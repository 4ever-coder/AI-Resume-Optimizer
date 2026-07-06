package com.qianyi.resumeoptimizer.resume;

import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

@Component
public class TextCleaner {

    private static final Pattern HIDDEN_CONTROL_CHARS = Pattern.compile("[\\p{Cntrl}&&[^\r\n\t]]");
    private static final Pattern SECTION_HEADING = Pattern.compile("^(教育经历|项目经历|实习经历|工作经历|技能|专业技能|证书|获奖|校园经历|个人总结)[:：]?$");

    public String clean(String rawText) {
        if (rawText == null) {
            return "";
        }

        String normalized = HIDDEN_CONTROL_CHARS.matcher(rawText)
                .replaceAll("")
                .replace('\u00A0', ' ')
                .replace("\r\n", "\n")
                .replace('\r', '\n')
                .replace('\t', ' ');

        List<String> lines = normalized.lines()
                .map(line -> line.replaceAll("\\s+", " ").trim())
                .filter(line -> !line.isBlank())
                .toList();

        return mergeBrokenLines(lines).trim();
    }

    private String mergeBrokenLines(List<String> lines) {
        List<String> merged = new ArrayList<>();
        StringBuilder current = new StringBuilder();

        for (String line : lines) {
            if (isSectionHeading(line) || looksLikeListItem(line)) {
                flush(current, merged);
                merged.add(line);
                continue;
            }
            if (current.isEmpty()) {
                current.append(line);
            } else if (current.length() < 80 && !endsSentence(current)) {
                current.append(' ').append(line);
            } else {
                flush(current, merged);
                current.append(line);
            }
        }
        flush(current, merged);
        return String.join("\n", merged);
    }

    private boolean isSectionHeading(String line) {
        return SECTION_HEADING.matcher(line).matches();
    }

    private boolean looksLikeListItem(String line) {
        return line.startsWith("-") || line.startsWith("•") || line.matches("^\\d+[.、].+");
    }

    private boolean endsSentence(StringBuilder text) {
        return text.toString().matches(".*[。！？.!?]$");
    }

    private void flush(StringBuilder current, List<String> merged) {
        if (!current.isEmpty()) {
            merged.add(current.toString());
            current.setLength(0);
        }
    }
}

