package es.wrapitup.wrapitup_planner.unit;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.fasterxml.jackson.databind.ObjectMapper;
import es.wrapitup.wrapitup_planner.dto.AiNoteResult;
import es.wrapitup.wrapitup_planner.service.OpenAiService;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

@Tag("unit")
public class OpenAiServiceUnitTest {

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Test
    void buildPromptIncludesEnglishKeysAndLanguageInstruction() throws Exception {
        OpenAiService service = new OpenAiService(objectMapper);
        setField(service, "maxOverviewChars", 600);
        setField(service, "maxSummaryChars", 4000);

        String prompt = (String) invokePrivate(service, "buildPrompt", new Class<?>[] { String.class }, new Object[] { "Sample text" });

        assertTrue(prompt.contains("\"title\""));
        assertTrue(prompt.contains("\"completeSummary\""));
        assertTrue(prompt.contains("same language as the original text"));
    }

    @Test
    void parseResponseTruncatesOverviewAndSummary() throws Exception {
        OpenAiService service = new OpenAiService(objectMapper);
        setField(service, "maxOverviewChars", 5);
        setField(service, "maxSummaryChars", 10);

        String content = "{\"title\":\"Title\",\"overview\":\"123456\",\"completeSummary\":\"1234567890123\"}";
        String response = "{\"choices\":[{\"message\":{\"content\":\"" + content.replace("\"", "\\\"") + "\"}}]}";

        AiNoteResult result = (AiNoteResult) invokePrivate(service, "parseResponse", new Class<?>[] { String.class }, new Object[] { response });

        assertEquals("Title", result.getTitle());
        assertEquals(5, result.getOverview().length());
        assertEquals(10, result.getCompleteSummary().length());
    }

    @Test
    void parseResponseWithoutChoicesThrowsException() throws Exception {
        OpenAiService service = new OpenAiService(objectMapper);

        assertThrows(InvocationTargetException.class, () -> {
            invokePrivate(service, "parseResponse", new Class<?>[] { String.class }, new Object[] { "{\"choices\":[]}" });
        });
    }

    @Test
    void generateNoteFromTextWithoutApiKeyThrowsException() throws Exception {
        OpenAiService service = new OpenAiService(objectMapper);
        setField(service, "apiKey", "");

        assertThrows(IllegalStateException.class, () -> service.generateNoteFromText("Some text"));
    }

    @Test
    void generateNoteFromTextWithBlankInputThrowsException() throws Exception {
        OpenAiService service = new OpenAiService(objectMapper);
        setField(service, "apiKey", "token");

        assertThrows(IllegalArgumentException.class, () -> service.generateNoteFromText(" "));
    }

    @Test
    void generateNoteFromTextTooLongThrowsException() throws Exception {
        OpenAiService service = new OpenAiService(objectMapper);
        setField(service, "apiKey", "token");
        setField(service, "maxInputChars", 3);

        assertThrows(IllegalArgumentException.class, () -> service.generateNoteFromText("abcd"));
    }

    private static void setField(Object target, String name, Object value) throws Exception {
        Field field = target.getClass().getDeclaredField(name);
        field.setAccessible(true);
        field.set(target, value);
    }

    private static Object invokePrivate(Object target, String methodName, Class<?>[] paramTypes, Object[] args) throws Exception {
        Method method = target.getClass().getDeclaredMethod(methodName, paramTypes);
        method.setAccessible(true);
        return method.invoke(target, args);
    }
}
