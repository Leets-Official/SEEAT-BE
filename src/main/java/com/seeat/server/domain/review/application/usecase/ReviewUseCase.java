package com.seeat.server.domain.review.application.usecase;

import com.seeat.server.domain.review.application.dto.request.ReviewRequest;
import com.seeat.server.domain.review.application.dto.request.ReviewUpdateRequest;
import com.seeat.server.domain.review.application.dto.response.ReviewDetailResponse;
import com.seeat.server.domain.review.application.dto.response.ReviewListResponse;
import com.seeat.server.domain.review.domain.entity.Review;
import com.seeat.server.global.response.pageable.PageRequest;
import com.seeat.server.global.response.pageable.SliceResponse;
import org.springframework.data.domain.Slice;

/**
 * [리뷰 인터페이스]
 * - 로그인한 유저만 좌석 후기를 작성 및 열람할 수 있습니다.
 * - 로그인한 유저만 좌석 클릭 시 해당 좌석의 후기 및 사진 열람 가능
 * -
 */
public interface ReviewUseCase {

    /// 리뷰 작성
    Review createReview(ReviewRequest request, Long userId);

    /// 리뷰 조회
    // 리뷰 상세 조회
    ReviewDetailResponse loadReview(Long reviewId);

    // 리뷰 목록 조회
    SliceResponse<ReviewListResponse> loadReviewsBySeatId(String seatId, PageRequest pageRequest);

    SliceResponse<ReviewListResponse> loadReviewsByAuditoriumId(String seatId, PageRequest pageRequest);

    SliceResponse<ReviewListResponse> loadFavoriteReviews(PageRequest pageRequest);

    /// 리뷰 수정
    void updateReview(ReviewUpdateRequest request, Long userId);

    /// 리뷰 삭제
    void deleteReview(Long reviewId, Long userId);


    /// 외부 의존성을 위한 유즈 케이스
    Slice<ReviewListResponse> loadReviewsForBookmark(Slice<Long> reviews);

    Review getReview(Long reviewId);
}

