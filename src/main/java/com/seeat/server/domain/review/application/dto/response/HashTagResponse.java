package com.seeat.server.domain.review.application.dto.response;

import com.seeat.server.domain.review.domain.entity.HashTag;
import lombok.Builder;
import java.util.List;

@Builder
public record HashTagResponse(
        Long hashTagId,
        String hashTagName,
        String hashTagType
) {

    /// 정적 팩토리 메서드
    public static HashTagResponse from(HashTag hashTag) {
        return HashTagResponse.builder()
                .hashTagId(hashTag.getId())
                .hashTagName(hashTag.getName())
                .hashTagType(hashTag.getType().getLabel())
                .build();
    }

    /// 정적 팩토리 메서드
    public static List<HashTagResponse> from(List<HashTag> hashTags) {
        return hashTags.stream()
                .map(HashTagResponse::from)
                .toList();
    }
}
