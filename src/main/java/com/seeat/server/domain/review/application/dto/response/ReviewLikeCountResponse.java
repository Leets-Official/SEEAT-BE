package com.seeat.server.domain.review.application.dto.response;

public record ReviewLikeCountResponse(
        long reviewCount,
        long likeCount
) {
}
