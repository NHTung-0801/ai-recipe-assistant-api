package com.project.recipe_assistant.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotEmpty;
import lombok.Data;
import java.util.List;

@Data
@Schema(description = "Yêu cầu gợi ý món ăn dựa trên danh sách nguyên liệu sẵn có")
public class IngredientRequest {

    @NotEmpty(message = "Danh sách nguyên liệu không được để trống")
    @Schema(
            description = "Danh sách nguyên liệu người dùng đang có trong tủ lạnh",
            example = "[\"ức gà\", \"bông cải xanh\", \"tỏi\", \"dầu olive\"]",
            requiredMode = Schema.RequiredMode.REQUIRED
    )
    private List<String> ingredients;

}
