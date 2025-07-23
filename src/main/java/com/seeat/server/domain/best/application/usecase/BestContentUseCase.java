package com.seeat.server.domain.best.application.usecase;

import com.seeat.server.domain.best.application.dto.response.BestReviewListResponse;
import com.seeat.server.domain.review.application.dto.response.ReviewListResponse;
import com.seeat.server.domain.best.application.dto.response.BestAuditoriumListResponse;
import com.seeat.server.global.response.pageable.PageRequest;
import com.seeat.server.global.response.pageable.SliceResponse;

/**
 * 기존 서비스들을 활용하여 인기가 있는 리뷰/상영관을 조회하는 인터페이스
 */
public interface BestContentUseCase {

    // 베스트 후기 목록 조회
    SliceResponse<BestReviewListResponse> loadBestReviews(PageRequest pageRequest);

    // 베스트 상영관 목록 조회
    SliceResponse<BestAuditoriumListResponse> loadBestTheaters(PageRequest pageRequest);

    // 스프링 배치
    void saveBestContents();

    /// 외부에서 사용할 레디스 및 스냅샷 삭제
    void deleteBestContents();

}
