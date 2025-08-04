package com.seeat.server.domain.review.domain.repository.dto;

import com.seeat.server.domain.review.domain.entity.Review;

/**
 * 리뷰와 좋아요를 동시에 받기 위한 DTO 인터페이스 입니다.
 */
public interface ReviewWithLikeCount {
    Review getReview();
    Long getLikeCount();
}
