package es.wrapitup.wrapitup_planner.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import es.wrapitup.wrapitup_planner.dto.AiNoteResult;
import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
public class OpenAiService {
    private final ObjectMapper objectMapper;
    private final HttpClient httpClient;

    @Value("${openai.api-key:}")
    private String apiKey;

    private static final String MODEL = "gpt-4o";

    @Value("${openai.api-base:https://api.openai.com/v1}")
    private String apiBase;

    @Value("${openai.max-input-chars:50000}")
    private int maxInputChars;

    @Value("${openai.max-overview-chars:600}")
    private int maxOverviewChars;

    @Value("${openai.max-summary-chars:5000}")
    private int maxSummaryChars;

    public OpenAiService(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
        this.httpClient = HttpClient.newBuilder().build();
    }

    public AiNoteResult generateNoteFromText(String text) {
        validateInput(text);

        String prompt = buildPrompt(text);
        String responseBody = callOpenAi(prompt);
        return parseResponse(responseBody);
    }

    public String generateQuizFromText(String text) {
        validateInput(text);
        String prompt = buildQuizPrompt(text);
        String responseBody = callOpenAi(prompt);
        return parseQuizResponse(responseBody);
    }

    private String buildPrompt(String text) {
        return "Analyze the following text and create a summary.\n" +
                "Return ONLY valid JSON with this exact structure:\n" +
                "{\n" +
                "  \"title\": \"Short title of the document\",\n" +
                "  \"overview\": \"Brief summary in 2-3 sentences\",\n" +
                "  \"completeSummary\": \"Detailed multi-paragraph summary around 4000 characters\",\n" +
                "  \"jsonQuestions\": {\n" +
                "    \"questions\": [\n" +
                "      {\n" +
                "        \"question\": \"Question text\",\n" +
                "        \"options\": [\"Option A\", \"Option B\", \"Option C\", \"Option D\"],\n" +
                "        \"correctOptionIndex\": 0\n" +
                "      }\n" +
                "    ]\n" +
                "  }\n" +
                "}\n" +
                "Write the summary in the same language as the original text.\n" +
                "Do NOT use phrases like 'this document', 'the text', 'the file', or similar meta-references.\n" +
                "Always refer directly to the topic, concepts, entities, events, formulas, or procedures found in the content.\n" +
                "The completeSummary must be detail-oriented and substantive:\n" +
                "- Explain important mechanisms, steps, causes/effects, definitions, and examples when present.\n" +
                "- Cover concrete details and key facts, not a generic overview.\n" +
                "- Avoid repeating the overview with slightly more words.\n" +
                "Generate between 5 and 10 multiple-choice questions.\n" +
                "Each question must include exactly 4 options and only one correct answer represented by correctOptionIndex (0-3).\n" +
                "Limits: title max 100 characters, overview max " + maxOverviewChars +
                " characters, completeSummary max " + maxSummaryChars + " characters.\n" +
                "Text:\n" +
                text;
    }

    private String buildQuizPrompt(String text) {
        return "Analyze the following text and create ONLY a multiple-choice quiz.\n" +
                "Return ONLY valid JSON with this exact structure:\n" +
                "{\n" +
                "  \"questions\": [\n" +
                "    {\n" +
                "      \"question\": \"Question text\",\n" +
                "      \"options\": [\"Option A\", \"Option B\", \"Option C\", \"Option D\"],\n" +
                "      \"correctOptionIndex\": 0\n" +
                "    }\n" +
                "  ]\n" +
                "}\n" +
                "Generate between 5 and 10 questions.\n" +
                "Each question must include exactly 4 options and only one correct answer represented by correctOptionIndex (0-3).\n" +
                "Use the same language as the original text.\n" +
                "Text:\n" +
                text;
    }

    private String callOpenAi(String prompt) {
        String requestBody = buildRequestBody(prompt);
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(apiBase + "/chat/completions"))
                .header("Authorization", "Bearer " + apiKey)
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(requestBody, StandardCharsets.UTF_8))
                .build();

        try {
            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
            if (response.statusCode() < 200 || response.statusCode() >= 300) {
                throw new IllegalStateException("OpenAI request failed with status " + response.statusCode());
            }
            return response.body();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new IllegalStateException("OpenAI call was interrupted", e);
        } catch (IOException e) {
            throw new IllegalStateException("Failed to call OpenAI", e);
        }
    }

    private void validateInput(String text) {
        if (apiKey == null || apiKey.isBlank()) {
            throw new IllegalStateException("OpenAI API key is not configured");
        }
        if (text == null || text.isBlank()) {
            throw new IllegalArgumentException("File content is empty");
        }
        if (text.length() > maxInputChars) {
            throw new IllegalArgumentException("File exceeds maximum character limit");
        }
    }

    private String buildRequestBody(String prompt) {
        ObjectNode root = objectMapper.createObjectNode();
        root.put("model", MODEL);
        root.put("temperature", 0.2);

        ArrayNode messages = root.putArray("messages");
        ObjectNode system = messages.addObject();
        system.put("role", "system");
        system.put("content", "Eres un asistente que devuelve solo JSON valido sin texto extra.");

        ObjectNode user = messages.addObject();
        user.put("role", "user");
        user.put("content", prompt);

        ObjectNode responseFormat = root.putObject("response_format");
        responseFormat.put("type", "json_object");

        return root.toString();
    }

    private AiNoteResult parseResponse(String body) {
        try {
            String content = extractContent(body);
            JsonNode contentRoot = objectMapper.readTree(content);

            AiNoteResult result = new AiNoteResult();
            result.setTitle(truncate(contentRoot.path("title").asText(""), 100));
            result.setOverview(truncate(contentRoot.path("overview").asText(""), maxOverviewChars));
            result.setCompleteSummary(truncate(contentRoot.path("completeSummary").asText(""), maxSummaryChars));

            JsonNode questionsNode = contentRoot.path("jsonQuestions");
            if (!questionsNode.isMissingNode() && !questionsNode.isNull()) {
                if (questionsNode.isTextual()) {
                    JsonNode parsedQuestions = objectMapper.readTree(questionsNode.asText());
                    validateQuizQuestions(parsedQuestions);
                    result.setJsonQuestions(parsedQuestions.toString());
                } else {
                    validateQuizQuestions(questionsNode);
                    result.setJsonQuestions(questionsNode.toString());
                }
            } else {
                result.setJsonQuestions("");
            }

            result.setTitle(truncate(result.getTitle(), 100));
            result.setOverview(truncate(result.getOverview(), maxOverviewChars));
            result.setCompleteSummary(truncate(result.getCompleteSummary(), maxSummaryChars));
            return result;
        } catch (IOException e) {
            throw new IllegalStateException("Failed to parse OpenAI response", e);
        }
    }

    private String parseQuizResponse(String body) {
        try {
            String content = extractContent(body);
            JsonNode root = objectMapper.readTree(content);
            validateQuizQuestions(root);
            return root.toString();
        } catch (IOException e) {
            throw new IllegalStateException("Failed to parse OpenAI response", e);
        }
    }

    private String extractContent(String body) throws IOException {
        JsonNode root = objectMapper.readTree(body);
        JsonNode choices = root.path("choices");
        if (!choices.isArray() || choices.isEmpty()) {
            throw new IllegalStateException("OpenAI response contains no choices");
        }
        JsonNode contentNode = choices.get(0).path("message").path("content");
        String content = contentNode.asText(null);
        if (content == null || content.isBlank()) {
            throw new IllegalStateException("OpenAI response is empty");
        }
        return content;
    }

    private void validateQuizQuestions(JsonNode root) {
        JsonNode questions = root.path("questions");
        if (!questions.isArray()) {
            throw new IllegalStateException("Quiz JSON must include a questions array");
        }

        int size = questions.size();
        if (size < 5 || size > 10) {
            throw new IllegalStateException("Quiz must include between 5 and 10 questions");
        }

        for (JsonNode questionNode : questions) {
            String questionText = questionNode.path("question").asText("").trim();
            if (questionText.isEmpty()) {
                throw new IllegalStateException("Each question must include non-empty question text");
            }

            JsonNode options = questionNode.path("options");
            if (!options.isArray() || options.size() != 4) {
                throw new IllegalStateException("Each question must include exactly 4 options");
            }

            for (JsonNode option : options) {
                if (option.asText("").trim().isEmpty()) {
                    throw new IllegalStateException("Question options cannot be empty");
                }
            }

            JsonNode correctOptionIndex = questionNode.path("correctOptionIndex");
            if (!correctOptionIndex.isInt()) {
                throw new IllegalStateException("Each question must include an integer correctOptionIndex");
            }

            int value = correctOptionIndex.asInt();
            if (value < 0 || value > 3) {
                throw new IllegalStateException("correctOptionIndex must be between 0 and 3");
            }
        }
    }

    private String truncate(String value, int maxLength) {
        if (value == null) {
            return "";
        }
        String trimmed = value.trim();
        if (trimmed.length() <= maxLength) {
            return trimmed;
        }
        return trimmed.substring(0, maxLength);
    }
}
