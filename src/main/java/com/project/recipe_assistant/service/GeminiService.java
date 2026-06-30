package com.project.recipe_assistant.service;

import com.project.recipe_assistant.exception.AiServiceException;

/**
 * Service chịu trách nhiệm giao tiếp với Google Gemini API.
 * Đây là tầng hạ tầng (Infrastructure-level), chỉ lo việc gửi/nhận chuỗi (raw text),
 * không hiểu gì về nghiệp vụ "công thức nấu ăn" — giúp dễ thay thế AI provider sau này.
 */
public interface GeminiService {

    /**
     * Gửi prompt đến Gemini và trả về phần text (JSON dạng String) mà model sinh ra.
     *
     * @param prompt nội dung prompt đã build sẵn (từ {@code PromptBuilder})
     * @return chuỗi text Gemini trả về (mong đợi là JSON theo định dạng đã yêu cầu)
     * @throws AiServiceException khi không kết nối được, key sai, hoặc response không hợp lệ
     */
    String generateContent(String prompt);
}
