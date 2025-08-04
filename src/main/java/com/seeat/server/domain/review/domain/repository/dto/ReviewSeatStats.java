package com.seeat.server.domain.review.domain.repository.dto;

import com.seeat.server.domain.theater.domain.entity.Seat;

public interface ReviewSeatStats {
    Seat getSeat();
    Long getReviewCount();
    Double getAverageRating();
}
