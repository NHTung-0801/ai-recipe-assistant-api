package com.project.recipe_assistant.controller;

import com.project.recipe_assistant.constant.AppConstants;
import com.project.recipe_assistant.dto.response.ApiResponse;
import com.project.recipe_assistant.dto.response.UserHistoryResponse;
import com.project.recipe_assistant.service.HistoryService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * REST endpoint truy vấn lịch sử các phiên gợi ý món ăn.
 * GET-only: history là dữ liệu chỉ đọc đối với client.
 */
@RestController
@RequestMapping(AppConstants.API_PREFIX_V1 + "/history")
@RequiredArgsConstructor
@Tag(name = "History", description = "API truy vấn lịch sử các phiên gợi ý đã thực hiện")
public class HistoryController {

    private final HistoryService historyService;

    /** GET /api/v1/history — danh sách tất cả phiên, mới nhất trước. */
    @Operation(summary = "Lấy danh sách lịch sử", description = "Trả về toàn bộ lịch sử các phiên gợi ý, sắp xếp mới nhất trước.")
    @GetMapping
    public ResponseEntity<ApiResponse<List<UserHistoryResponse>>> getAll() {
        List<UserHistoryResponse> data = historyService.getAllHistory();
        ApiResponse<List<UserHistoryResponse>> response = ApiResponse.<List<UserHistoryResponse>>builder()
                .status(HttpStatus.OK.value())
                .message(AppConstants.MSG_SUCCESS)
                .data(data)
                .build();
        return ResponseEntity.ok(response);
    }

    /** GET /api/v1/history/{id} — chi tiết 1 phiên lịch sử. */
    @Operation(summary = "Lấy chi tiết 1 phiên lịch sử theo id")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200", description = "Trả về chi tiết phiên lịch sử"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "404", description = "Không tìm thấy id")
    })
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<UserHistoryResponse>> getById(
            @Parameter(description = "MongoDB ObjectId của phiên lịch sử", example = "65f1a2b3c4d5e6f7a8b9c0d1")
            @PathVariable String id) {
        UserHistoryResponse data = historyService.getHistoryById(id);
        ApiResponse<UserHistoryResponse> response = ApiResponse.<UserHistoryResponse>builder()
                .status(HttpStatus.OK.value())
                .message(AppConstants.MSG_SUCCESS)
                .data(data)
                .build();
        return ResponseEntity.ok(response);
    }
}
