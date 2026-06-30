package com.project.recipe_assistant.constant;

public class AppConstants {

    // Đường dẫn gốc cho tất cả các API
    public static final String API_PREFIX_V1 = "/api/v1";

    // Các thông báo phản hồi chuẩn
    public static final String MSG_SUCCESS = "Xử lý thành công";
    public static final String MSG_VALIDATION_ERROR = "Dữ liệu đầu vào không hợp lệ";
    public static final String MSG_AI_ERROR = "Lỗi khi kết nối hoặc xử lý dữ liệu từ AI";
    public static final String MSG_NOT_FOUND = "Không tìm thấy dữ liệu yêu cầu";

    public static final String GEMINI_SYSTEM_PROMPT =
            "You are an expert culinary assistant. Based on the provided ingredients, suggest 2-3 recipes. " +
                    "Return the response STRICTLY in the following JSON format: " +
                    "{ \"suggestedRecipes\": [ { \"name\": \"\", \"ingredients\": [\"\"], \"instructions\": [\"\"], \"preparationTime\": 0, \"estimatedCalories\": 0, \"protein\": 0.0, \"carbs\": 0.0, \"fat\": 0.0, \"tags\": [\"\"] } ] }";

    // Tên cache (Spring Cache name), dùng cho @Cacheable - tránh hard-code chuỗi rải rác
    public static final String CACHE_RECIPE_SUGGESTIONS = "recipeSuggestions";

    // Ngăn không cho khởi tạo object từ class này vì nó chỉ chứa các hằng số static
    private AppConstants() {}
}