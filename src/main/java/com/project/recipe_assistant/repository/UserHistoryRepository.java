package com.project.recipe_assistant.repository;

import com.project.recipe_assistant.model.UserHistory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface UserHistoryRepository extends MongoRepository<UserHistory, String> {

    // Lấy toàn bộ lịch sử, sắp xếp mới nhất trước (giữ lại cho test/internal use)
    List<UserHistory> findAllByOrderBySearchTimeDesc();

    // Lấy lịch sử có phân trang. Pageable do client truyền vào - chứa cả page, size, sort.
    // Spring Data Mongo tự xử lý OFFSET/LIMIT và tính totalElements.
    Page<UserHistory> findBy(Pageable pageable);
}
