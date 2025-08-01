package com.seeat.server.domain.review.application.usecase;

import com.seeat.server.domain.review.application.dto.response.AuditoriumHashTagResponse;
import com.seeat.server.domain.review.domain.entity.Review;
import com.seeat.server.domain.review.domain.entity.ReviewHashTag;

import java.util.*;

/**
 * [리뷰 해시태그 인터페이스]
 * - 리뷰에 해시태그가 들어가도록 정의하는 인터페이스 입니다.
 */

public interface ReviewHashTagUseCase {

    // ========================
    //  저장 함수
    // ========================
    /// 리뷰 해시태그 작성
    void createReviewHashTag(Review review, List<Long> hashTags);

    // ========================
    //  조회 함수
    // ========================

    /// 상영관의 정보에 따른 해시태그 목록 조회하기
    List<AuditoriumHashTagResponse> loadReviewHashTagsByAuditoriumId(String auditoriumId);

    // ========================
    //  삭제 함수
    // ========================
    /// 리뷰 해시태그 삭제
    void deleteReviewHashTagByReviewId(Long reviewId);


    // ========================
    //  외부 함수
    // ========================
    /// 리뷰에 따른 해시태그 목록 조회
    List<ReviewHashTag> getReviewHashTagByReview(Review review);

    /// 리뷰 ID 목록에 따른 해시태그 목록 조회
    List<ReviewHashTag> getReviewHashTagByReviews(List<Long> reviewIds);

}
