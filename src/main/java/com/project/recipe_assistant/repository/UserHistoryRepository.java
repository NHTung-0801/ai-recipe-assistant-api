package com.project.recipe_assistant.repository;

import com.project.recipe_assistant.model.UserHistory;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface UserHistoryRepository extends MongoRepository<UserHistory, String> {

    // Lấy danh sách lịch sử và sắp xếp theo thời gian tìm kiếm mới nhất giảm dần
    List<UserHistory> findAllByOrderBySearchTimeDesc();
}