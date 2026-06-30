package com.project.recipe_assistant.service.impl;

import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;
import com.project.recipe_assistant.exception.AiServiceException;
import com.project.recipe_assistant.service.GeminiService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

import java.util.List;
import java.util.Map;

/**
 * Triển khai {@link GeminiService} dùng {@link RestClient} (Spring 6+).
 * <p>
 * Đọc {@code gemini.api.url} và {@code gemini.api.key} từ application properties
 * (cuối cùng được nạp từ biến môi trường {@code GEMINI_API_KEY}).
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class GeminiServiceImpl implements GeminiService {

    private final RestClient restClient;
    private final ObjectMapper objectMapper;

    @Value("${gemini.api.url}")
    private String geminiApiUrl;

    @Value("${gemini.api.key}")
    private String geminiApiKey;

    @Override
    public String generateContent(String prompt) {
        // Cấu trúc request body theo chuẩn Gemini REST API:
        // { "contents": [ { "parts": [ { "text": "<prompt>" } ] } ] }
        Map<String, Object> requestBody = Map.of(
                "contents", List.of(
                        Map.of("parts", List.of(
                                Map.of("text", prompt)
                        ))
                )
        );

        try {
            log.debug("Gửi prompt tới Gemini API. Độ dài prompt: {} ký tự", prompt.length());

            // Gemini cho phép truyền API key qua header "x-goog-api-key" — an toàn và sạch URL hơn query param.
            String rawResponse = restClient.post()
                    .uri(geminiApiUrl)
                    .header("x-goog-api-key", geminiApiKey)
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(requestBody)
                    .retrieve()
                    .body(String.class);

            if (rawResponse == null || rawResponse.isBlank()) {
                throw new AiServiceException("Gemini API trả về response rỗng");
            }

            return extractTextFromResponse(rawResponse);

        } catch (RestClientException ex) {
            // Bắt mọi lỗi network/HTTP của RestClient (4xx, 5xx, timeout, DNS,...) và đóng gói lại.
            log.error("Lỗi khi gọi Gemini API: {}", ex.getMessage());
            throw new AiServiceException("Không thể kết nối đến Gemini API", ex);
        }
    }

    /**
     * Bóc tách phần text mà Gemini sinh ra khỏi response wrapper.
     * <p>
     * Cấu trúc response thực tế:
     * <pre>
     * {
     *   "candidates": [
     *     { "content": { "parts": [ { "text": "..." } ] } }
     *   ]
     * }
     * </pre>
     * Dùng {@link JsonNode} thay vì DTO cứng để dễ chống chịu khi Google đổi schema phụ.
     */
    private String extractTextFromResponse(String rawResponse) {
        try {
            JsonNode root = objectMapper.readTree(rawResponse);
            JsonNode textNode = root
                    .path("candidates").path(0)
                    .path("content")
                    .path("parts").path(0)
                    .path("text");

            if (textNode.isMissingNode() || textNode.asString().isBlank()) {
                throw new AiServiceException("Response của Gemini không có trường 'text' hợp lệ: " + rawResponse);
            }
            return textNode.asString();

        } catch (AiServiceException ex) {
            throw ex;
        } catch (Exception ex) {
            throw new AiServiceException("Không thể parse response từ Gemini API", ex);
        }
    }
}
