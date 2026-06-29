package com.project.recipe_assistant.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "user_histories")
public class UserHistory {

    @Id
    private String id;

    // Danh sách nguyên liệu người dùng đã nhập vào (vd: ["thịt bò", "hành tây", "cà chua"])
    private List<String> requestedIngredients;

    // Danh sách các công thức (Recipes) mà AI đã phân tích và trả về.
    private List<Recipe> suggestedRecipes;

    // Tự động ghi nhận thời gian truy vấn (Yêu cầu phải có @EnableMongoAuditing ở class config)
    @CreatedDate
    private LocalDateTime searchTime;
}