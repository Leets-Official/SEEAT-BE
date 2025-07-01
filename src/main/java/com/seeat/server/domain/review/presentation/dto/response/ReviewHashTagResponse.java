package com.seeat.server.domain.review.presentation.dto.response;


import com.seeat.server.domain.review.domain.entity.ReviewHashTag;
import lombok.Builder;

import java.util.List;

/**
 * 리뷰-해시태그 매핑 정보 응답 DTO
 *
 * @param id           리뷰-해시태그 매핑 고유 ID
 * @param reviewId     리뷰 ID
 * @param hashTagId    해시태그 ID
 * @param hashTagName  해시태그 이름
 */

@Builder
public record ReviewHashTagResponse(
        Long id,
        Long reviewId,
        Long hashTagId,
        String hashTagName
) {
    public static ReviewHashTagResponse from(ReviewHashTag reviewHashTag) {
        return ReviewHashTagResponse.builder()
                .id(reviewHashTag.getId())
                .reviewId(reviewHashTag.getReview().getId())
                .hashTagId(reviewHashTag.getHashTag().getId())
                .hashTagName(reviewHashTag.getHashTag().getName())
                .build();
    }

    public static List<ReviewHashTagResponse> from(List<ReviewHashTag> reviewHashTags) {
        return reviewHashTags.stream()
                .map(ReviewHashTagResponse::from)
                .toList();
    }
}

