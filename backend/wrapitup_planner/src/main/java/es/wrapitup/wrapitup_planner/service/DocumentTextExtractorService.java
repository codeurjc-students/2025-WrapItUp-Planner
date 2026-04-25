package es.wrapitup.wrapitup_planner.service;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.Locale;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;
import org.apache.poi.xslf.usermodel.XMLSlideShow;
import org.apache.poi.xslf.usermodel.XSLFShape;
import org.apache.poi.xslf.usermodel.XSLFSlide;
import org.apache.poi.xslf.usermodel.XSLFTextShape;
import org.apache.poi.xwpf.usermodel.XWPFDocument;
import org.apache.poi.xwpf.usermodel.XWPFParagraph;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

@Service
public class DocumentTextExtractorService {

    public String extractText(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new IllegalArgumentException("File is required");
        }

        String extension = getExtension(file.getOriginalFilename());
        switch (extension) {
            case "txt":
            case "md":
                return readPlainText(file);
            case "pdf":
                return readPdf(file);
            case "docx":
                return readDocx(file);
            case "pptx":
                return readPptx(file);
            default:
                throw new IllegalArgumentException("Unsupported file type. Use PDF, Word, PowerPoint, TXT, or MD.");
        }
    }

    private String readPlainText(MultipartFile file) {
        try {
            String content = new String(file.getBytes(), StandardCharsets.UTF_8).trim();
            if (content.isBlank()) {
                throw new IllegalArgumentException("File is empty or unreadable");
            }
            return content;
        } catch (IOException e) {
            throw new IllegalArgumentException("Unable to read file", e);
        }
    }

    private String readPdf(MultipartFile file) {
        try (InputStream inputStream = file.getInputStream(); PDDocument document = PDDocument.load(inputStream)) {
            PDFTextStripper stripper = new PDFTextStripper();
            String text = stripper.getText(document).trim();
            if (text.isBlank()) {
                throw new IllegalArgumentException("PDF contains no readable text");
            }
            return text;
        } catch (IOException e) {
            throw new IllegalArgumentException("Unable to read PDF", e);
        }
    }

    private String readDocx(MultipartFile file) {
        try (InputStream inputStream = file.getInputStream(); XWPFDocument document = new XWPFDocument(inputStream)) {
            StringBuilder builder = new StringBuilder();
            for (XWPFParagraph paragraph : document.getParagraphs()) {
                String text = paragraph.getText();
                if (text != null && !text.isBlank()) {
                    builder.append(text).append('\n');
                }
            }
            String result = builder.toString().trim();
            if (result.isBlank()) {
                throw new IllegalArgumentException("Word document contains no readable text");
            }
            return result;
        } catch (IOException e) {
            throw new IllegalArgumentException("Unable to read Word document", e);
        }
    }

    private String readPptx(MultipartFile file) {
        try (InputStream inputStream = file.getInputStream(); XMLSlideShow slideShow = new XMLSlideShow(inputStream)) {
            StringBuilder builder = new StringBuilder();
            for (XSLFSlide slide : slideShow.getSlides()) {
                for (XSLFShape shape : slide.getShapes()) {
                    if (shape instanceof XSLFTextShape) {
                        String text = ((XSLFTextShape) shape).getText();
                        if (text != null && !text.isBlank()) {
                            builder.append(text).append('\n');
                        }
                    }
                }
            }
            String result = builder.toString().trim();
            if (result.isBlank()) {
                throw new IllegalArgumentException("PowerPoint contains no readable text");
            }
            return result;
        } catch (IOException e) {
            throw new IllegalArgumentException("Unable to read PowerPoint", e);
        }
    }

    private String getExtension(String filename) {
        if (filename == null || filename.isBlank()) {
            return "";
        }
        int lastDot = filename.lastIndexOf('.');
        if (lastDot < 0 || lastDot == filename.length() - 1) {
            return "";
        }
        return filename.substring(lastDot + 1).toLowerCase(Locale.ROOT);
    }
}
