package com.seeat.server.domain.review.application.dto.response;

import com.seeat.server.domain.review.domain.entity.Review;
import lombok.Builder;
import java.util.List;

@Builder
public record ReviewSaveResponse(Long reviewId) {


    /// 정적 팩토리 메서드
    public static ReviewSaveResponse from(Review review) {
        return ReviewSaveResponse.builder()
                .reviewId(review.getId())
                .build();
    }

    /// 정적 팩토리 메서드
    public static List<ReviewSaveResponse> from(List<Review> reviews){
        return reviews.stream()
                .map(ReviewSaveResponse::from)
                .toList();
    }

}
