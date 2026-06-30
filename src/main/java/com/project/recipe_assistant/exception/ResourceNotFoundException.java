package com.project.recipe_assistant.exception;

/**
 * Ngoại lệ dùng khi truy vấn một resource (history, recipe,...) bằng id nhưng không tồn tại trong DB.
 * Được {@code GlobalExceptionHandler} bắt và trả về HTTP 404 + ApiResponse chuẩn.
 */
public class ResourceNotFoundException extends RuntimeException {
    public ResourceNotFoundException(String message) {
        super(message);
    }
}
