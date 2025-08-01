package com.seeat.server.domain.review.presentation.swagger;

import com.seeat.server.domain.review.application.dto.response.ReviewSummaryResponse;
import com.seeat.server.global.response.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@Tag(name = "AI 요약 API", description = "상영관의 리뷰들에 대해서 요약하는 API입니다.")
public interface ReviewSummaryControllerSpec {

    /**
     * 상영관에 따른 리뷰 요약 API
     *
     * @param auditoriumId 영화관 ID
     */
    @Operation(
            summary = "상영관별 리뷰 AI 요약 조회",
            description = "AI를 통해, 상영관 ID로 모든 리뷰 목록을 요약합니다."
    )
    @GetMapping("/auditorium/{auditoriumId}")
    ApiResponse<ReviewSummaryResponse> getReviewSummaryByAuditorium(
            @Parameter(description = "조회할 상영관ID", example = "13018")
            @PathVariable String auditoriumId);

}
