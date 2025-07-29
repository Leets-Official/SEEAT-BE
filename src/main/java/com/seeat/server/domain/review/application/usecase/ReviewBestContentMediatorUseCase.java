package com.seeat.server.domain.review.application.usecase;

import com.seeat.server.domain.best.application.dto.response.BestReviewListResponse;
import com.seeat.server.global.response.pageable.PageRequest;
import com.seeat.server.global.response.pageable.SliceResponse;

/**
 * 순환의존성을 끊기 위해 리뷰 서비스와 분리하여 새로운 인터페이스 정의
 */
public interface ReviewBestContentMediatorUseCase {

    SliceResponse<BestReviewListResponse> getBestReviews(PageRequest pageRequest);

}
