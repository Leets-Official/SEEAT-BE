package com.seeat.server.domain.review.application.dto.request;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class BookmarkRequest {
    private Long reviewId;
    private Long userId;
}
