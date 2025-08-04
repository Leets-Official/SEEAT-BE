package com.seeat.server.domain.review.application.usecase;

import com.seeat.server.domain.review.application.dto.request.ReviewRequest;
import com.seeat.server.domain.review.application.dto.request.ReviewSortType;
import com.seeat.server.domain.review.application.dto.request.ReviewUpdateRequest;
import com.seeat.server.domain.review.application.dto.response.ReviewDetailResponse;
import com.seeat.server.domain.review.application.dto.response.ReviewListResponse;
import com.seeat.server.domain.review.application.dto.response.ReviewSeatListResponse;
import com.seeat.server.domain.review.domain.entity.Review;
import com.seeat.server.global.response.pageable.PageRequest;
import com.seeat.server.global.response.pageable.SliceResponse;
import org.springframework.data.domain.Slice;

import java.io.IOException;

/**
 * [리뷰 인터페이스]
 * - 로그인한 유저만 좌석 후기를 작성 및 열람할 수 있습니다.
 * - 로그인한 유저만 좌석 클릭 시 해당 좌석의 후기 및 사진 열람 가능
 * -
 */
public interface ReviewUseCase {

    // ========================
    //  저장 함수
    // ========================

    Review createReview(ReviewRequest request, Long userId) throws IOException;

    // ========================
    //  조회 함수
    // ========================
    /// 상세 조회
    ReviewDetailResponse loadReview(Long reviewId);

    /// 좌석 바탕 리뷰 목록 조회
    SliceResponse<ReviewSeatListResponse> loadReviewsBySeatId(
            String seatId, PageRequest pageRequest, ReviewSortType sort);

    /// 상영관 바탕 리뷰 목록 조회
    SliceResponse<ReviewListResponse> loadReviewsByAuditoriumId(
            String auditoriumId, PageRequest pageRequest, ReviewSortType sort);

    // ========================
    //  수정 함수
    // ========================

    /// 리뷰 수정
    void updateReview(Long reviewId, ReviewUpdateRequest request, Long userId) throws IOException;

    // ========================
    //  삭제 함수
    // ========================

    /// 리뷰 삭제
    void deleteReview(Long reviewId, Long userId) throws IOException;

    // ========================
    //  외부 함수
    // ========================

    /// 리뷰 예외처리
    Review getReview(Long reviewId);

    /// 외부 의존성을 위한 유즈 케이스
    Slice<ReviewListResponse> loadReviewsForBookmark(Slice<Long> reviews);

    /// 나의 후기 목록 조회하기
    SliceResponse<ReviewListResponse> loadMyReviews(Long userId, PageRequest pageRequest);

    Long countsReviewsByAuditoriumId(String auditoriumId);
}

