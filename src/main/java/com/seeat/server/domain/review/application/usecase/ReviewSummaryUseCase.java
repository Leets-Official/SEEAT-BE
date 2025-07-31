package com.seeat.server.domain.review.application.usecase;

import com.seeat.server.domain.review.application.dto.response.ReviewSummaryResponse;

public interface ReviewSummaryUseCase {

    /// 상영관에 따른 후기 요약
    ReviewSummaryResponse loadSummaryByAuditoriumId(String auditoriumId);

}
