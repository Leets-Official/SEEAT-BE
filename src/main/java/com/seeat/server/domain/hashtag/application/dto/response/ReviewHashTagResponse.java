package com.seeat.server.domain.hashtag.application.dto.response;


import com.seeat.server.domain.hashtag.domain.entity.ReviewHashTag;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;

import java.util.List;

/**
 * 리뷰-해시태그 매핑 정보 응답 DTO
 *
 * @param hashTagId    해시태그 ID
 * @param hashTagName  해시태그 이름
 */

@Builder
@Schema(name = "[응답][리뷰] 해시태그 매핑 정보 Response", description = "리뷰에 연결된 해시태그 정보를 담고 있는 DTO입니다.")
public record ReviewHashTagResponse(
        @Schema(description = "해시태그 ID", example = "1")
        Long hashTagId,

        @Schema(description = "해시태그 이름", example = "감동")
        String hashTagName,

        @Schema(description = "해시태그 타입", example = "음향")
        String hashTagType
) {
    public static ReviewHashTagResponse from(ReviewHashTag reviewHashTag) {
        return ReviewHashTagResponse.builder()
                .hashTagId(reviewHashTag.getHashTag().getId())
                .hashTagName(reviewHashTag.getHashTag().getName())
                .hashTagType(reviewHashTag.getHashTag().getType().getLabel())
                .build();
    }

    public static List<ReviewHashTagResponse> from(List<ReviewHashTag> reviewHashTags) {
        return reviewHashTags.stream()
                .map(ReviewHashTagResponse::from)
                .toList();
    }
}

