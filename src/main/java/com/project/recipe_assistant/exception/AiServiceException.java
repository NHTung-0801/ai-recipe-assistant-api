package com.project.recipe_assistant.exception;

/**
 * Ngoại lệ tùy chỉnh (Custom Exception) dành riêng cho các lỗi liên quan đến AI.
 * Được sử dụng (throw) khi:
 * - Hệ thống không thể kết nối đến Gemini API (mất mạng, timeout).
 * - API key không hợp lệ hoặc bị từ chối.
 * - Dữ liệu Gemini trả về bị lỗi hoặc không thể parse được sang JSON.
 */

public class AiServiceException extends RuntimeException {
    public AiServiceException(String message) {
        super(message);
    }

    public AiServiceException(String message, Throwable cause) {
        super(message, cause);
    }
}
