package com.seeat.server.domain.review.domain;

import com.seeat.server.domain.review.domain.entity.Review;
import com.seeat.server.domain.review.domain.entity.ReviewLike;
import com.seeat.server.domain.user.domain.entity.User;

public class ReviewLikeFixtures {

    public static ReviewLike stub(User user, Review review) {
        return ReviewLike.builder()
                .user(user)
                .review(review)
                .build();
    }

}
