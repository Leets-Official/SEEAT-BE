package com.seeat.server.domain.search.application.dto.response;
import com.seeat.server.domain.review.domain.entity.Review;
import lombok.Builder;

import java.util.List;

@Builder
public record ReviewSearchResponse(
        Long reviewId,
        String content,
        int rating,
        String movieTitle,
        String thumbnailUrl,
        String theaterName,
        String auditoriumName,
        String seatPosition,
        String userNickname,
        List<String> hashTags
) {

}
