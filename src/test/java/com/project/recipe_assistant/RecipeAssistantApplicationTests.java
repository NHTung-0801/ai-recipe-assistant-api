package com.project.recipe_assistant;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

/**
 * Smoke test: chỉ verify Spring context khởi tạo thành công.
 * <p>
 * Dùng profile "test" để load {@code application-test.yml} - tắt Mongo auto-config
 * và dùng giá trị giả cho Gemini key, giúp test chạy được trên CI mà không cần Docker.
 */
@SpringBootTest
@ActiveProfiles("test")
class RecipeAssistantApplicationTests {

	@Test
	void contextLoads() {
	}

}
