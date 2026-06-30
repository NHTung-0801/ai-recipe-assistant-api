package com.project.recipe_assistant.exception;

import com.project.recipe_assistant.constant.AppConstants;
import com.project.recipe_assistant.dto.response.ApiResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.HashMap;
import java.util.Map;

/**
 * Lớp xử lý lỗi toàn cục (Global Exception Handler).
 * Nhiệm vụ: Tự động "bắt" (catch) tất cả các lỗi văng ra từ Controller,
 * sau đó đóng gói lại thành chuẩn ApiResponse để Frontend luôn nhận được định dạng JSON đồng nhất.
 */
@RestControllerAdvice
public class GlobalExceptionHandler {

    /**
     * Bắt lỗi: AiServiceException
     * Trường hợp xảy ra: Khi class GeminiService gặp sự cố trong quá trình giao tiếp với AI.
     * Xử lý: Trả về mã lỗi 503 (Service Unavailable) thông báo dịch vụ AI đang gián đoạn.
     */
    @ExceptionHandler(AiServiceException.class)
    public ResponseEntity<ApiResponse<Object>> handleAiServiceException(AiServiceException ex) {
        ApiResponse<Object> response = ApiResponse.builder()
                .status(HttpStatus.SERVICE_UNAVAILABLE.value())
                .message(AppConstants.MSG_AI_ERROR + ": " + ex.getMessage())
                .build();
        return new ResponseEntity<>(response, HttpStatus.SERVICE_UNAVAILABLE);
    }

    /**
     * Bắt lỗi: MethodArgumentNotValidException
     * Trường hợp xảy ra: Khi dữ liệu client gửi lên (DTO) vi phạm các rule kiểm tra (VD: @NotEmpty ở IngredientRequest).
     * Xử lý: Trả về mã lỗi 400 (Bad Request) kèm theo danh sách chi tiết các trường bị nhập sai hoặc thiếu.
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiResponse<Object>> handleValidationException(MethodArgumentNotValidException ex) {
        Map<String, String> errors = new HashMap<>();
        ex.getBindingResult().getFieldErrors().forEach(error ->
                errors.put(error.getField(), error.getDefaultMessage()));

        ApiResponse<Object> response = ApiResponse.builder()
                .status(HttpStatus.BAD_REQUEST.value())
                .message(AppConstants.MSG_VALIDATION_ERROR)
                .data(errors)
                .build();
        return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
    }

    /**
     * Bắt lỗi: ResourceNotFoundException
     * Trường hợp xảy ra: Truy vấn theo id (vd: UserHistory id) nhưng không tìm thấy trong DB.
     * Xử lý: Trả về 404 Not Found.
     */
    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<ApiResponse<Object>> handleResourceNotFound(ResourceNotFoundException ex) {
        ApiResponse<Object> response = ApiResponse.builder()
                .status(HttpStatus.NOT_FOUND.value())
                .message(ex.getMessage())
                .build();
        return new ResponseEntity<>(response, HttpStatus.NOT_FOUND);
    }

    /**
     * Bắt lỗi: Exception (Lỗi chung / Lỗi hệ thống)
     * Trường hợp xảy ra: Bắt tất cả các lỗi không lường trước được (NullPointerException, rớt database,...).
     * Xử lý: Trả về mã lỗi 500 (Internal Server Error), đóng vai trò như chốt chặn cuối cùng để server không bị crash.
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiResponse<Object>> handleGeneralException(Exception ex) {
        ApiResponse<Object> response = ApiResponse.builder()
                .status(HttpStatus.INTERNAL_SERVER_ERROR.value())
                .message(ex.getMessage())
                .build();
        return new ResponseEntity<>(response, HttpStatus.INTERNAL_SERVER_ERROR);
    }
}