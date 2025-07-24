package com.seeat.server.domain.bookmark.application.dto.request;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class BookmarkRequest {
    private Long reviewId;
    private Long userId;
}
