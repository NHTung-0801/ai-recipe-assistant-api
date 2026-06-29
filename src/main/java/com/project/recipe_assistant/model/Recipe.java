package com.project.recipe_assistant.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "recipes")
public class Recipe {

    @Id
    private String id;

    private String name; // Tên món ăn

    private List<String> ingredients; // Danh sách nguyên liệu chi tiết

    // Sử dụng List<String> thay vì String cho instructions để dễ dàng hiển thị từng bước nấu ăn trên UI
    private List<String> instructions;

    private int preparationTime; // Thời gian chuẩn bị và nấu (phút)

    // Nhóm chỉ số dinh dưỡng (Macros) cực kỳ quan trọng cho người tự nấu ăn và tập luyện
    private int estimatedCalories; // Tổng lượng Calo ước tính
    private double protein;        // Lượng Đạm (gram)
    private double carbs;          // Lượng Tinh bột (gram)
    private double fat;            // Lượng Chất béo (gram)

    private List<String> tags; // Gắn thẻ phân loại (vd: "Tăng cơ", "Ít béo", "Ăn sáng", "Nhanh gọn")
}