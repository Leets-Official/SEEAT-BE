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
@Schema(name = "[응답][영화관] 평점/개수 포함 좌석 배치도 Response", description = "상영관 내 좌석의 평점 요약을 제공하는 응답 DTO")
public record SeatRatingSummaryResponse(

        @Schema(description = "좌석 ID", example = "13018A4")
        String seatId,

        @Schema(description = "좌석 행(Row)", example = "A")
        String row,

        @Schema(description = "좌석 열(Column)", example = "4")
        int column,

        @Schema(description = "리뷰 개수", example = "100")
        int totalReviews,

        @Schema(description = "평균 점수", example = "3.5")
        double averageRating,

        @Schema(description = "휠체어 좌석 여부", example = "false")
        boolean isWheelchair,

        @Schema(description = "좌석의 상태")
        SeatType type

) {

    /// 정적 팩토리 메서드
    public static SeatRatingSummaryResponse of(Seat seat, Integer totalReviews, Float averageRating) {

        // 좌석 추출
        return SeatRatingSummaryResponse.builder()
                .seatId(seat.getId())
                .row(seat.getRow())
                .column(seat.getColumn())
                .totalReviews(totalReviews == null ? 0 : totalReviews)
                .averageRating(averageRating == null ? 0.0 : averageRating)
                .isWheelchair(false)
                .type(getType(totalReviews, averageRating))
                .build();
    }

    /// enum 추출하기 위한 변수
    private static SeatType getType(Integer totalReviews, Float averageRating) {

        if (totalReviews == null || totalReviews == 0 || averageRating == null || averageRating == 0) {
            return SeatType.NO_REVIEW;
        } else if (averageRating > 4.5) {
            return SeatType.HIGH_RATED;
        } else if (averageRating < 1.5) {
            return SeatType.LOW_RATED;
        } else {
            return SeatType.REVIEWED;
        }
    }
}
