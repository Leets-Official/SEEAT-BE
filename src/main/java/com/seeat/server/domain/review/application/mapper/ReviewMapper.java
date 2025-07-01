package com.seeat.server.domain.review.application.mapper;

import com.seeat.server.domain.review.domain.entity.Review;
import com.seeat.server.domain.review.presentation.dto.request.ReviewRequest;
import org.springframework.stereotype.Component;

@Component
public class ReviewMapper {

    public Review toEntity(ReviewRequest request) {
        return Review.builder()
                .build();
    }

}
