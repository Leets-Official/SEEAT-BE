package com.seeat.server.domain.theater.application.dto.response;

import com.seeat.server.domain.theater.domain.entity.Auditorium;
import com.seeat.server.domain.theater.domain.repository.dto.AuditoriumWithRating;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;

/**
 * 상영관 관련 상세 조회 DTO
 * @param theaterName       영화관 제목
 * @param auditoriumId      상영관 ID
 * @param auditoriumName    상영관 이름
 * @param screenSize        스크린 이름
 * @param soundType         사운드 타입
 * @param reviewCount       후기 개수
 * @param averageReview     평균 평점
 */
@Schema(name = "상영관 DTO", description = "상영관 상세 정보 응답 DTO")
@Builder
public record AuditoriumDetailResponse(

        @Schema(description = "영화관 이름", example = "메가박스 강남")
        String theaterName,

        @Schema(description = "상영관 ID", example = "AUD123")
        String auditoriumId,

        @Schema(description = "상영관 이름", example = "1관 IMAX")
        String auditoriumName,

        @Schema(description = "스크린 크기 또는 타입", example = "IMAX Laser")
        String screenSize,

        @Schema(description = "사운드 타입", example = "Dolby Atmos")
        String soundType,

        @Schema(description = "총 리뷰 수", example = "124")
        Integer reviewCount,

        @Schema(description = "평균 평점 (0.0 ~ 5.0)", example = "4.5")
        Float averageReview

) {

    /// 정적 팩토리 메서드
    public static AuditoriumDetailResponse from(AuditoriumWithRating withRating) {

        Auditorium auditorium = withRating.getAuditorium();
        Integer reviewCount = withRating.getTotalReviews();
        Float averageReview = withRating.getAverageRating();

        return AuditoriumDetailResponse.builder()
                .theaterName(auditorium.getTheater().getName())
                .auditoriumId(auditorium.getId())
                .auditoriumName(auditorium.getName())
                .screenSize(auditorium.getScreenSize())
                .soundType(auditorium.getSoundType())
                .reviewCount(reviewCount == null ? 0 : reviewCount)
                .averageReview(averageReview == null ? 0 : averageReview)
                .build();
    }
}
