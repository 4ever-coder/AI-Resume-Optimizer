package com.qianyi.resumeoptimizer.resume;

import org.apache.pdfbox.Loader;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;
import org.apache.poi.xwpf.usermodel.XWPFDocument;
import org.apache.poi.xwpf.usermodel.XWPFTable;
import org.springframework.stereotype.Service;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Locale;
import java.util.stream.Collectors;

@Service
public class ResumeParserService {

    private final ResumeProperties properties;
    private final TextCleaner textCleaner;

    public ResumeParserService(ResumeProperties properties, TextCleaner textCleaner) {
        this.properties = properties;
        this.textCleaner = textCleaner;
    }

    public ParsedResume parse(byte[] content, String extension) {
        try {
            return switch (extension.toLowerCase(Locale.ROOT)) {
                case "pdf" -> parsePdf(content);
                case "docx" -> parseDocx(content);
                case "txt" -> parseTxt(content);
                default -> throw new ResumeParseException("仅支持 PDF、DOCX、TXT 简历文件。", "UNSUPPORTED_FILE_TYPE");
            };
        } catch (ResumeParseException exception) {
            throw exception;
        } catch (IOException exception) {
            throw new ResumeParseException("简历解析失败，请确认文件没有损坏或加密。", "PARSE_FAILED");
        }
    }

    private ParsedResume parsePdf(byte[] content) throws IOException {
        try (PDDocument document = Loader.loadPDF(content)) {
            if (document.getNumberOfPages() > properties.maxPdfPages()) {
                throw new ResumeParseException("PDF 页数过多，请上传 " + properties.maxPdfPages() + " 页以内的简历。", "PDF_TOO_MANY_PAGES");
            }
            String text = new PDFTextStripper().getText(document);
            return clean(text, document.getNumberOfPages());
        }
    }

    private ParsedResume parseDocx(byte[] content) throws IOException {
        try (XWPFDocument document = new XWPFDocument(new ByteArrayInputStream(content))) {
            String paragraphs = document.getParagraphs().stream()
                    .map(paragraph -> paragraph.getText())
                    .collect(Collectors.joining("\n"));
            String tables = document.getTables().stream()
                    .map(this::tableText)
                    .collect(Collectors.joining("\n"));
            return clean(paragraphs + "\n" + tables, Math.max(1, document.getProperties().getExtendedProperties().getUnderlyingProperties().getPages()));
        }
    }

    private ParsedResume parseTxt(byte[] content) {
        return clean(new String(content, StandardCharsets.UTF_8), 1);
    }

    private ParsedResume clean(String rawText, int pageCount) {
        String cleaned = textCleaner.clean(rawText);
        if (cleaned.length() < properties.minTextChars()) {
            throw new ResumeParseException("简历文本为空或有效内容过少，请检查文件内容是否为可复制文本。", "EMPTY_RESUME_TEXT");
        }
        return new ParsedResume(cleaned, pageCount);
    }

    private String tableText(XWPFTable table) {
        return table.getRows().stream()
                .flatMap(row -> row.getTableCells().stream())
                .map(cell -> cell.getText())
                .collect(Collectors.joining("\n"));
    }
}

