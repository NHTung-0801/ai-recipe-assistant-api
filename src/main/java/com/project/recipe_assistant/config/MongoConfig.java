package com.project.recipe_assistant.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.data.mongodb.config.EnableMongoAuditing;

@Configuration
@EnableMongoAuditing // Bật tính năng tự động ghi nhận thời gian (CreatedDate, LastModifiedDate)
public class MongoConfig {

    // Hiện tại chỉ cần annotation @EnableMongoAuditing là đủ.
    // Nếu sau này cần viết các Converter tùy chỉnh (ví dụ lưu Enum phức tạp vào MongoDB),
    // bạn sẽ khai báo các Bean đó tại đây.
}