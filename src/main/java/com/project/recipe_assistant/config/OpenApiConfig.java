package com.project.recipe_assistant.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.servers.Server;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

/**
 * Cấu hình metadata cho tài liệu OpenAPI / Swagger UI.
 * <p>
 * Truy cập sau khi app chạy:
 * <ul>
 *   <li>Swagger UI: {@code http://localhost:8080/swagger-ui/index.html}</li>
 *   <li>OpenAPI JSON: {@code http://localhost:8080/v3/api-docs}</li>
 * </ul>
 */
@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI recipeAssistantOpenAPI() {
        Info info = new Info()
                .title("Home-Cooked Recipe Assistant API")
                .version("v1.0.0")
                .description("""
                        API gợi ý món ăn tự nấu tốt cho sức khỏe dựa trên nguyên liệu sẵn có.
                        Mỗi công thức trả về kèm chỉ số dinh dưỡng (Calories, Protein, Carbs, Fat)
                        và thời gian chuẩn bị — phù hợp người ăn lành mạnh / tập gym.
                        """)
                .contact(new Contact()
                        .name("NHTung")
                        .url("https://github.com/NHTung-0801"))
                .license(new License()
                        .name("MIT License")
                        .url("https://opensource.org/licenses/MIT"));

        Server localServer = new Server()
                .url("http://localhost:8080")
                .description("Local development server");

        return new OpenAPI()
                .info(info)
                .servers(List.of(localServer));
    }
}
