package com.seeat.server.domain.review.application.usecase;

import com.seeat.server.domain.review.domain.entity.Review;
import com.seeat.server.domain.review.domain.entity.ReviewHashTag;

import java.util.*;

/**
 * [리뷰 해시태그 인터페이스]
 * - 리뷰에 해시태그가 들어가도록 정의하는 인터페이스 입니다.
 */

public interface ReviewHashTagUseCase {

    /// 리뷰 해시태그 작성
    void createReviewHashTag(Review review, List<Long> hashTags);

    /// 리뷰 해시태그 조회
    // 리뷰에 따른 해시태그 목록 조회
    List<ReviewHashTag> getReviewHashTagByReview(Review review);

    // 리뷰 ID 목록에 따른 해시태그 목록 조회
    List<ReviewHashTag> getReviewHashTagByReviews(List<Long> reviewIds);

    /// 리뷰 해시태그 삭제
    void deleteReviewHashTag(Long reviewHashTagId);

}
