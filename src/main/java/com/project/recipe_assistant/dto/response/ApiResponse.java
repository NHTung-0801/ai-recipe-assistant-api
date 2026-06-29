package com.project.recipe_assistant.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ApiResponse<T> {
    private int status;           // Mã HTTP (200, 400, 500...)
    private String message;       // Thông báo ngắn gọn
    private T data;               // Khối dữ liệu thực tế (Generic)

    @Builder.Default
    private LocalDateTime timestamp = LocalDateTime.now(); // Thời gian phản hồi
}