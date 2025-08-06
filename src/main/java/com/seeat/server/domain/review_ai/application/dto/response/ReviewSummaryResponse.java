package com.seeat.server.domain.review_ai.application.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;

/**
 * 리뷰 요약 응답 DTO
 * @param auditoriumId  상영관 ID
 * @param summary       AI에 의해서 요약된 정보
 */
@Builder
@Schema(name = "[응답][요약] 상영관 요약 정보 Response", description = "리뷰 요약에 사용되는 DTO 입니다.")
public record ReviewSummaryResponse(

        @Schema(description = "상영관 ID", example = "13084")
        String auditoriumId,

        @Schema(description = "상영관 이름", example = "CGV 용산아이파크몰 IMAX관")
        String auditoriumName,

        @Schema(description = "요약 내용", example = "해당 상영관은 시야와 음향이 좋은 것이 특징이며, 편안한 좌석과 접근성이 좋은 것으로 유저들에게 인기가 있습니다. 주로 액션 영화나 스포츠 관련 영화를 선호하는 것으로 보입니다. 후기의 전반적인 감성은 긍정적이며, 자주 사용하는 해시태그는 시야와 음향에 관련된 것이 많습니다. 또한 작은 규모의 극장이지만 위치가 좋아서 찾기 쉽고, 혼자 조용히 영화를 즐기기에도 좋은 것으로 보입니다.")
        String summary
) {

    /// 정적 팩토리 메서드
    public static ReviewSummaryResponse from(String auditoriumId, String auditoriumName, String summary) {

        return ReviewSummaryResponse.builder()
                .auditoriumId(auditoriumId)
                .auditoriumName(auditoriumName)
                .summary(summary)
                .build();
    }

}
