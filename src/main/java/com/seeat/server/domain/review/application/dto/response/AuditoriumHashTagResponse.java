package com.seeat.server.domain.review.application.dto.response;

import com.seeat.server.domain.review.domain.entity.ReviewHashTag;
import com.seeat.server.domain.review.domain.repository.dto.ReviewHashTagWithCount;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import java.util.List;

/**
 * 상영관 리뷰들 중에서 많이 사용된 해시태그를 파악하기 위한 DTO 입니다.
 * @param hashTagId     해시태그 ID
 * @param hashTagName   해시태그 이름
 * @param count         개수
 */
@Builder
@Schema(name = "[응답][상영관] 상영관 해시태그 Response",description = "상영관에서 자주 사용되는 해시태그 목록에 대한 DTO 입니다.")
public record AuditoriumHashTagResponse(
        Long hashTagId,
        String hashTagName,
        Long count
) {

    /// 정적 팩토리 메서드
    public static AuditoriumHashTagResponse from(ReviewHashTagWithCount withCount) {
        return AuditoriumHashTagResponse.builder()
                .hashTagId(withCount.getHashTagId())
                .hashTagName(withCount.getHashTagName())
                .count(withCount.getCount())
                .build();


    }

    /// 정적 팩토리 메서드
    public static List<AuditoriumHashTagResponse> from(List<ReviewHashTagWithCount> reviewHashTags) {

        return reviewHashTags.stream()
                .map(AuditoriumHashTagResponse::from)
                .toList();

    }
}
