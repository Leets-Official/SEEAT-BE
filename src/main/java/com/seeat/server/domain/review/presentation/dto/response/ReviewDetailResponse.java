package com.seeat.server.domain.review.presentation.dto.response;

import com.seeat.server.domain.review.domain.entity.Review;
import com.seeat.server.domain.review.domain.entity.ReviewHashTag;
import com.seeat.server.domain.user.presentation.dto.response.UserResponse;
import lombok.Builder;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 리뷰 상세조회 응답 DTO
 *
 * @param movieSeatInfo 영화 및 좌석 정보 DTO
 * @param hashtags      해시태그 목록 DTO
 * @param content       리뷰 내용
 * @param rating        평점 (1~5)
 * @param user          작성자 정보 DTO
 * @param createdAt     리뷰 작성 시간
 */

@Builder
public record ReviewDetailResponse(
        ReviewSeatInfoResponse movieSeatInfo,
        List<ReviewHashTagResponse> hashtags,
        String content,
        double rating,
        UserResponse user,
        LocalDateTime createdAt
) {
    /**
     * Review 및 연관 엔티티로부터 ReviewDetailResponse 생성
     */

    public static ReviewDetailResponse from(
            Review review,
            List<ReviewHashTag> hashTags
    ) {
        return ReviewDetailResponse.builder()
                .movieSeatInfo(ReviewSeatInfoResponse
                        .from(review))
                .hashtags(ReviewHashTagResponse
                        .from(hashTags))
                .content(review.getContent())
                .rating(review.getRating())
                .user(UserResponse
                        .from(review.getUser()))
                .createdAt(review.getCreatedAt())
                .build();
    }
}
