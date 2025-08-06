package com.seeat.server.domain.review_ai.presentation;

import com.seeat.server.domain.review_ai.application.dto.response.ReviewSummaryResponse;
import com.seeat.server.domain.review_ai.application.usecase.ReviewSummaryUseCase;
import com.seeat.server.domain.review_ai.presentation.swagger.ReviewSummaryControllerSpec;
import com.seeat.server.global.response.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/summary")
public class ReviewSummaryController implements ReviewSummaryControllerSpec {

    /// 요약 기능
    private final ReviewSummaryUseCase summaryService;

    /**
     * 상영관에 따른 리뷰 요약 조회
     *
     * @param auditoriumId 상영관에 따른 리뷰 요약 조회를 위한 Id
     */
    @GetMapping("/{auditoriumId}")
    public ApiResponse<ReviewSummaryResponse> getReviewSummaryByAuditorium(
            @PathVariable String auditoriumId) {

        // 서비스 호출
        ReviewSummaryResponse response = summaryService.loadSummaryByAuditoriumId(auditoriumId);

        // 결과 리턴
        return ApiResponse.ok(response);
    }

}
