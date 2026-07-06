package com.qianyi.resumeoptimizer.resume;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.font.PDType1Font;
import org.apache.pdfbox.pdmodel.font.Standard14Fonts;
import org.apache.poi.xwpf.usermodel.XWPFDocument;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayOutputStream;
import java.nio.charset.StandardCharsets;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class ResumeParserServiceTests {

    private final ResumeParserService parserService =
            new ResumeParserService(new ResumeProperties(8 * 1024 * 1024, 20, 20), new TextCleaner());

    @Test
    void parsesTxtAndKeepsSectionClues() {
        ParsedResume resume = parserService.parse("""
                教育经历

                北京大学 软件工程
                项目经历
                - 负责订单系统接口优化
                技能
                Java Spring Boot MySQL
                """.getBytes(StandardCharsets.UTF_8), "txt");

        assertThat(resume.text()).contains("教育经历", "项目经历", "技能");
        assertThat(resume.pageCount()).isEqualTo(1);
    }

    @Test
    void parsesDocxText() throws Exception {
        XWPFDocument document = new XWPFDocument();
        document.createParagraph().createRun().setText("项目经历");
        document.createParagraph().createRun().setText("Built resume optimizer service with Java and Spring Boot.");
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        document.write(output);
        document.close();

        ParsedResume resume = parserService.parse(output.toByteArray(), "docx");

        assertThat(resume.text()).contains("项目经历", "Spring Boot");
    }

    @Test
    void parsesPdfText() throws Exception {
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        try (PDDocument document = new PDDocument()) {
            PDPage page = new PDPage();
            document.addPage(page);
            try (PDPageContentStream stream = new PDPageContentStream(document, page)) {
                stream.beginText();
                stream.setFont(new PDType1Font(Standard14Fonts.FontName.HELVETICA), 12);
                stream.newLineAtOffset(50, 700);
                stream.showText("Project Experience Built Java API with Spring Boot and MySQL.");
                stream.endText();
            }
            document.save(output);
        }

        ParsedResume resume = parserService.parse(output.toByteArray(), "pdf");

        assertThat(resume.text()).contains("Project Experience", "Spring Boot");
        assertThat(resume.pageCount()).isEqualTo(1);
    }

    @Test
    void rejectsEmptyText() {
        assertThatThrownBy(() -> parserService.parse("   ".getBytes(StandardCharsets.UTF_8), "txt"))
                .isInstanceOf(ResumeParseException.class)
                .hasMessageContaining("有效内容过少");
    }
}

