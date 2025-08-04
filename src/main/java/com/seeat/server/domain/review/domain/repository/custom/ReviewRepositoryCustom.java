package com.seeat.server.domain.review.domain.repository.custom;

import com.seeat.server.domain.review.domain.entity.Review;
import com.seeat.server.domain.search.application.dto.request.ReviewSearchCondition;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;

/**
 * 커스텀 리뷰 레포지토리 인터페이스
 *
 * 다양한 필터(검색어, 상영관, 해시태그)와 정렬 조건을 적용한
 * 리뷰 검색 기능을 위한 메서드를 선언합니다.
 */
public interface ReviewRepositoryCustom {

    /**
     * 검색 조건에 따라 리뷰를 조회합니다.
     *
     * @param condition 검색 조건 DTO
     * @param pageable 페이징
     * @return Slice<Review> 응답
     */
    Slice<Review> searchReviewsWithFilters(ReviewSearchCondition condition, Pageable pageable);
}
