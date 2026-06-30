package com.project.recipe_assistant.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * Cấu hình CORS cho REST API.
 * <p>
 * Danh sách origin được đọc từ properties {@code cors.allowed-origins} - dạng comma-separated.
 * Cho phép thay đổi theo môi trường mà không cần build lại jar (dev: localhost:5173,
 * prod: domain frontend thật).
 */
@Configuration
public class CorsConfig implements WebMvcConfigurer {

    @Value("${cors.allowed-origins}")
    private String[] allowedOrigins;

    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/api/**") // Chỉ áp CORS cho REST API, không cho swagger/static
                .allowedOriginPatterns(allowedOrigins) // Pattern hỗ trợ wildcard, an toàn khi dùng credentials
                .allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS")
                .allowedHeaders("*")
                .allowCredentials(true) // Cho phép cookie/auth header - chuẩn bị cho Spring Security sau này
                .maxAge(3600); // Cache preflight 1 giờ - giảm OPTIONS request
    }
}
