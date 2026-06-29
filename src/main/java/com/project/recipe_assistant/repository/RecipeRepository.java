package com.project.recipe_assistant.repository;

import com.project.recipe_assistant.model.Recipe;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface RecipeRepository extends MongoRepository<Recipe, String> {

    // Spring Data MongoDB tự động hỗ trợ các hàm như save(), findById(), findAll(), deleteById()

    // Tìm danh sách món ăn có chứa một thẻ tag cụ thể (ví dụ: "Ít béo")
    List<Recipe> findByTagsContaining(String tag);
}