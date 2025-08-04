package com.seeat.server.domain.review.domain.repository.dto;

public record ReviewLikeCount(
        long reviewCount,
        long likeCount
) {
}

