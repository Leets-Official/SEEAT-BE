package com.seeat.server.domain.review.application.dto.response;

import com.seeat.server.domain.review.domain.entity.Review;
import com.seeat.server.domain.review.domain.entity.ReviewHashTag;
import com.seeat.server.domain.review.domain.entity.ReviewImage;
import com.seeat.server.domain.user.application.dto.response.UserResponse;
import com.seeat.server.global.image.application.dto.response.ImageInfoResponse;
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
 * @param heartCount    좋아요 개수
 * @param imageInfo     이미지 목록 DTO
 * @param createdAt     리뷰 작성 시간
 */

@Builder
public record ReviewDetailResponse(
        ReviewSeatInfoResponse movieSeatInfo,
        List<ReviewHashTagResponse> hashtags,
        String content,
        double rating,
        UserResponse user,
        List<ImageInfoResponse> imageInfo,
        Long heartCount,
        LocalDateTime createdAt
) {

    public static ReviewDetailResponse from(
            Review review,
            List<ReviewHashTag> hashTags, Long heartCount, List<ReviewImage> images
    ) {

        return ReviewDetailResponse.builder()
                .movieSeatInfo(ReviewSeatInfoResponse
                        .from(review))
                .hashtags(ReviewHashTagResponse
                        .from(hashTags))
                .content(review.getContent())
                .rating(review.getRating())
                .heartCount(heartCount)
                .user(UserResponse
                        .from(review.getUser()))
                .imageInfo(ImageInfoResponse.from(images))
                .createdAt(review.getCreatedAt())
                .build();
    }
}
