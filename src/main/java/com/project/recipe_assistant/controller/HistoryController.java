package com.project.recipe_assistant.controller;

import com.project.recipe_assistant.constant.AppConstants;
import com.project.recipe_assistant.dto.response.ApiResponse;
import com.project.recipe_assistant.dto.response.PageResponse;
import com.project.recipe_assistant.dto.response.UserHistoryResponse;
import com.project.recipe_assistant.service.HistoryService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * REST endpoint truy vấn lịch sử các phiên gợi ý món ăn.
 */
@RestController
@RequestMapping(AppConstants.API_PREFIX_V1 + "/history")
@RequiredArgsConstructor
@Tag(name = "History", description = "API truy vấn lịch sử các phiên gợi ý đã thực hiện")
public class HistoryController {

    private final HistoryService historyService;

    /**
     * GET /api/v1/history?page=0&size=10&sort=searchTime,desc
     * <p>
     * Spring tự bind query params vào Pageable. {@code @PageableDefault} đặt default
     * khi client không truyền: page 0, size 10, sort searchTime giảm dần.
     */
    @Operation(
            summary = "Lấy danh sách lịch sử (có phân trang)",
            description = "Trả về lịch sử các phiên gợi ý, mặc định sort theo searchTime giảm dần. Client có thể truyền ?page, ?size, ?sort để tuỳ chỉnh."
    )
    @GetMapping
    public ResponseEntity<ApiResponse<PageResponse<UserHistoryResponse>>> getAll(
            @PageableDefault(size = 10, sort = "searchTime", direction = Sort.Direction.DESC)
            Pageable pageable) {

        PageResponse<UserHistoryResponse> data = historyService.getHistory(pageable);
        ApiResponse<PageResponse<UserHistoryResponse>> response =
                ApiResponse.<PageResponse<UserHistoryResponse>>builder()
                        .status(HttpStatus.OK.value())
                        .message(AppConstants.MSG_SUCCESS)
                        .data(data)
                        .build();
        return ResponseEntity.ok(response);
    }

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

    @Operation(summary = "Xóa 1 phiên lịch sử theo id")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "204", description = "Xóa thành công"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "404", description = "Không tìm thấy id")
    })
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable String id) {
        historyService.deleteHistory(id);
        // 204 No Content - chuẩn REST cho DELETE thành công, không cần body
        return ResponseEntity.noContent().build();
    }
}
