package com.seeat.server.domain.search.application.usecase;

import com.seeat.server.domain.search.application.dto.request.ReviewSearchCondition;
import com.seeat.server.domain.search.application.dto.response.ReviewSearchResponse;
import com.seeat.server.global.response.pageable.PageRequest;
import com.seeat.server.global.response.pageable.SliceResponse;

public interface ReviewSearchUseCase {

    // 인기순, 평점순, 최신순 및 필터 조회
    SliceResponse<ReviewSearchResponse> getReviewList(
            ReviewSearchCondition condition, String guestToken,
            Long userId, PageRequest pageRequest);
}
