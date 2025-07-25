package com.seeat.server.domain.review.application.dto.response;

import com.seeat.server.domain.review.domain.entity.HashTag;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import java.util.List;

/**
 * 해시태그 DTO
 * @param hashTagId     해시태그 ID
 * @param hashTagName   해시태그 이름
 * @param hashTagType   해시태그 타입
 */
@Builder
@Schema(name = "[응답] 해시태그 Response",description = "해시태그 목록에 대한 DTO 입니다.")
public record HashTagResponse(

        @Schema(description = "해시태그 ID", example = "1")
        Long hashTagId,

        @Schema(description = "해시태그 이름", example = "최고의 음향")
        String hashTagName,

        @Schema(description = "해시태그 종류", example = "음향")
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
