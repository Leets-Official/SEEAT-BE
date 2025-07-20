package com.seeat.server.domain.theater.application.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;

import com.seeat.server.domain.theater.domain.entity.Seat;

/**
 * 좌석 평점을 나타내는 DTO
 * @param seatId            좌석ID
 * @param row               행
 * @param column            열
 * @param totalReviews      리뷰 개수
 * @param averageRating     평균 점수
 * @param isWheelchair      휠체어 좌석인지
 */
@Builder
@Schema(name = "평점/개수 포함 좌석 배치도 DTO", description = "상영관 내 좌석의 평점 요약을 제공하는 응답 DTO")
public record SeatRatingSummaryResponse(

        @Schema(description = "좌석ID")
        String seatId,

        @Schema(description = "행")
        String row,

        @Schema(description = "열")
        int column,

        @Schema(description = "리뷰 개수")
        int totalReviews,

        @Schema(description = "평균 점수")
        float averageRating,

        @Schema(description = "휠체어 좌석 여부")
        boolean isWheelchair

) {

    /// 정적 팩토리 메서드
    public static SeatRatingSummaryResponse of(Seat seat, Integer totalReviews, Float averageRating) {
        // 좌석 추출
        return SeatRatingSummaryResponse.builder()
                .seatId(seat.getId())
                .row(seat.getRow())
                .column(seat.getColumn())
                .totalReviews(totalReviews == null ? 0 : totalReviews)
                .averageRating(averageRating == null ? 0.0f : averageRating)
                .isWheelchair(false)
                .build();
    }
}
