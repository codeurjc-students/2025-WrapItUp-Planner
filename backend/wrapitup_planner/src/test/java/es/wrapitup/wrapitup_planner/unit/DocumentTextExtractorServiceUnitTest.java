package es.wrapitup.wrapitup_planner.unit;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import es.wrapitup.wrapitup_planner.service.DocumentTextExtractorService;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.font.PDType1Font;
import org.apache.poi.xslf.usermodel.XMLSlideShow;
import org.apache.poi.xslf.usermodel.XSLFSlide;
import org.apache.poi.xslf.usermodel.XSLFTextBox;
import org.apache.poi.xwpf.usermodel.XWPFDocument;
import org.apache.poi.xwpf.usermodel.XWPFParagraph;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockMultipartFile;

@Tag("unit")
public class DocumentTextExtractorServiceUnitTest {

    private final DocumentTextExtractorService service = new DocumentTextExtractorService();

    @Test
    void extractTextFromPlainTextFile() {
        MockMultipartFile file = new MockMultipartFile(
            "file",
            "note.txt",
            "text/plain",
            "Hello from AI test".getBytes()
        );

        String result = service.extractText(file);

        assertEquals("Hello from AI test", result);
    }

    @Test
    void extractTextFromEmptyFileThrowsException() {
        MockMultipartFile file = new MockMultipartFile(
            "file",
            "note.txt",
            "text/plain",
            new byte[0]
        );

        assertThrows(IllegalArgumentException.class, () -> service.extractText(file));
    }

    @Test
    void extractTextFromUnsupportedFileThrowsException() {
        MockMultipartFile file = new MockMultipartFile(
            "file",
            "note.xyz",
            "application/octet-stream",
            "content".getBytes()
        );

        assertThrows(IllegalArgumentException.class, () -> service.extractText(file));
    }

    @Test
    void extractTextFromPdfFile() throws IOException {
        byte[] data;
        try (PDDocument document = new PDDocument()) {
            PDPage page = new PDPage();
            document.addPage(page);

            try (PDPageContentStream contentStream = new PDPageContentStream(document, page)) {
                contentStream.beginText();
                contentStream.setFont(PDType1Font.HELVETICA, 12);
                contentStream.newLineAtOffset(50, 700);
                contentStream.showText("PDF content");
                contentStream.endText();
            }

            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            document.save(outputStream);
            data = outputStream.toByteArray();
        }

        MockMultipartFile file = new MockMultipartFile(
            "file",
            "note.pdf",
            "application/pdf",
            data
        );

        String result = service.extractText(file);

        assertTrue(result.contains("PDF content"));
    }

    @Test
    void extractTextFromDocxFile() throws IOException {
        byte[] data;
        try (XWPFDocument document = new XWPFDocument()) {
            XWPFParagraph paragraph = document.createParagraph();
            paragraph.createRun().setText("DOCX content");

            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            document.write(outputStream);
            data = outputStream.toByteArray();
        }

        MockMultipartFile file = new MockMultipartFile(
            "file",
            "note.docx",
            "application/vnd.openxmlformats-officedocument.wordprocessingml.document",
            data
        );

        String result = service.extractText(file);

        assertTrue(result.contains("DOCX content"));
    }

    @Test
    void extractTextFromPptxFile() throws IOException {
        byte[] data;
        try (XMLSlideShow slideShow = new XMLSlideShow()) {
            XSLFSlide slide = slideShow.createSlide();
            XSLFTextBox textBox = slide.createTextBox();
            textBox.setText("PPTX content");

            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            slideShow.write(outputStream);
            data = outputStream.toByteArray();
        }

        MockMultipartFile file = new MockMultipartFile(
            "file",
            "note.pptx",
            "application/vnd.openxmlformats-officedocument.presentationml.presentation",
            data
        );

        String result = service.extractText(file);

        assertTrue(result.contains("PPTX content"));
    }
    @Test
    void extractTextWhenFileIsNullThrowsException() {
        assertThrows(IllegalArgumentException.class, () ->
            service.extractText(null)
        );
    }
    @Test
    void extractTextWithNullFilenameThrowsUnsupported() {
        MockMultipartFile file = new MockMultipartFile(
            "file",
            null,
            "text/plain",
            "content".getBytes()
        );

        assertThrows(IllegalArgumentException.class, () ->
            service.extractText(file)
        );
    }
    
    @Test
    void extractTextWithNoExtensionThrowsException() {
        MockMultipartFile file = new MockMultipartFile(
            "file",
            "file",
            "text/plain",
            "content".getBytes()
        );

        assertThrows(IllegalArgumentException.class, () ->
            service.extractText(file)
        );
    }

    @Test
    void extractPdfWithNoTextThrowsException() throws Exception {

        PDDocument document = new PDDocument();
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        document.save(outputStream);
        document.close();

        MockMultipartFile file = new MockMultipartFile(
            "file",
            "empty.pdf",
            "application/pdf",
            outputStream.toByteArray()
        );

        assertThrows(IllegalArgumentException.class, () ->
            service.extractText(file)
        );
    }
    @Test
    void extractDocxWithNoTextThrowsException() throws Exception {

        XWPFDocument doc = new XWPFDocument();
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        doc.write(out);
        doc.close();

        MockMultipartFile file = new MockMultipartFile(
            "file",
            "empty.docx",
            "application/vnd.openxmlformats-officedocument.wordprocessingml.document",
            out.toByteArray()
        );

        assertThrows(IllegalArgumentException.class, () ->
            service.extractText(file)
        );
    }
    @Test
    void extractPptxWithNoTextThrowsException() throws Exception {

        XMLSlideShow ppt = new XMLSlideShow();
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        ppt.write(out);
        ppt.close();

        MockMultipartFile file = new MockMultipartFile(
            "file",
            "empty.pptx",
            "application/vnd.openxmlformats-officedocument.presentationml.presentation",
            out.toByteArray()
        );

        assertThrows(IllegalArgumentException.class, () ->
            service.extractText(file)
        );
    }

}
