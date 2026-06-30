package com.project.recipe_assistant.service.impl;

import com.project.recipe_assistant.exception.AiServiceException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestClient;
import tools.jackson.databind.ObjectMapper;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

/**
 * Unit test cho {@link GeminiServiceImpl}.
 * <p>
 * Stub từng cấp của fluent chain RestClient một cách tường minh (không dùng
 * RETURNS_DEEP_STUBS vì gây fragile với varargs trong {@code .header(name, value...)}).
 * <p>
 * {@link ReflectionTestUtils#setField} dùng để inject URL/key trực tiếp vì
 * trong unit test không có Spring context để resolve {@code @Value}.
 */
@ExtendWith(MockitoExtension.class)
class GeminiServiceImplTest {

    @Mock private RestClient restClient;
    @Mock private RestClient.RequestBodyUriSpec uriSpec;
    @Mock private RestClient.RequestBodySpec bodySpec;
    @Mock private RestClient.ResponseSpec responseSpec;

    private final ObjectMapper objectMapper = new ObjectMapper();

    private GeminiServiceImpl geminiService;

    @BeforeEach
    void setUp() {
        geminiService = new GeminiServiceImpl(restClient, objectMapper);
        ReflectionTestUtils.setField(geminiService, "geminiApiUrl", "http://fake-url");
        ReflectionTestUtils.setField(geminiService, "geminiApiKey", "fake-key");

        // Stub fluent chain - trả về chính bodySpec cho mọi bước trung gian.
        // Header dùng anyString() (cả name lẫn vararg value, Mockito sẽ match đúng 1 vararg).
        when(restClient.post()).thenReturn(uriSpec);
        when(uriSpec.uri(anyString())).thenReturn(bodySpec);
        when(bodySpec.header(anyString(), anyString())).thenReturn(bodySpec);
        when(bodySpec.contentType(any(MediaType.class))).thenReturn(bodySpec);
        when(bodySpec.body(any(Object.class))).thenReturn(bodySpec);
        when(bodySpec.retrieve()).thenReturn(responseSpec);
    }

    @Test
    @DisplayName("generateContent: bóc đúng text trong candidates[0].content.parts[0].text")
    void generateContent_shouldExtractTextFromCandidates() {
        String geminiResponse = """
                {
                  "candidates": [
                    {"content": {"parts": [{"text": "RECIPE_JSON_HERE"}]}}
                  ]
                }
                """;
        when(responseSpec.body(String.class)).thenReturn(geminiResponse);

        String result = geminiService.generateContent("any prompt");

        assertThat(result).isEqualTo("RECIPE_JSON_HERE");
    }

    @Test
    @DisplayName("generateContent: ném AiServiceException khi response không có candidates")
    void generateContent_shouldThrow_whenNoCandidates() {
        when(responseSpec.body(String.class)).thenReturn("""
                {"error": "quota exceeded"}
                """);

        assertThatThrownBy(() -> geminiService.generateContent("prompt"))
                .isInstanceOf(AiServiceException.class)
                .hasMessageContaining("text");
    }

    @Test
    @DisplayName("generateContent: ném AiServiceException khi response rỗng")
    void generateContent_shouldThrow_whenResponseEmpty() {
        when(responseSpec.body(String.class)).thenReturn("");

        assertThatThrownBy(() -> geminiService.generateContent("prompt"))
                .isInstanceOf(AiServiceException.class)
                .hasMessageContaining("rỗng");
    }

    @Test
    @DisplayName("generateContent: ném AiServiceException khi network lỗi (timeout/connection refused)")
    void generateContent_shouldWrapNetworkError() {
        // RestClient ném ResourceAccessException khi timeout, DNS fail, connection refused...
        when(responseSpec.body(String.class))
                .thenThrow(new ResourceAccessException("connection refused"));

        assertThatThrownBy(() -> geminiService.generateContent("prompt"))
                .isInstanceOf(AiServiceException.class)
                .hasMessageContaining("Không thể kết nối");
    }

    @Test
    @DisplayName("generateContent: ném AiServiceException khi response không phải JSON hợp lệ")
    void generateContent_shouldThrow_whenInvalidJson() {
        when(responseSpec.body(String.class)).thenReturn("<html>error page</html>");

        assertThatThrownBy(() -> geminiService.generateContent("prompt"))
                .isInstanceOf(AiServiceException.class);
    }
}
