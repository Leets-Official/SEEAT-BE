package com.seeat.server.domain.review.domain;

import com.seeat.server.domain.review.domain.entity.HashTag;
import com.seeat.server.domain.review.domain.entity.Review;
import com.seeat.server.domain.review.domain.entity.ReviewHashTag;

public class ReviewHashTagFixtures {
    public static ReviewHashTag createReviewHashTag(Review review, HashTag hashTag) {
        return ReviewHashTag.builder()
                .review(review)
                .hashTag(hashTag)
                .build();
    }
}
