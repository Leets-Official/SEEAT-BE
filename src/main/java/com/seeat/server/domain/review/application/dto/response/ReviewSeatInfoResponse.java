package com.seeat.server.domain.review.application.dto.response;

import com.seeat.server.domain.review.domain.entity.Review;
import com.seeat.server.domain.theater.domain.entity.Seat;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;

/**
 * 관람영화 및 좌석정보 DTO
 *
 * @param movieTitle     영화 제목
 * @param theaterName    영화관 이름
 * @param auditoriumName 상영관 이름
 * @param seatNumber     좌석 번호 (예: "F12")
 */
@Builder
@Schema(name = "[응답][리뷰] 좌석/영화/상영관 Response", description = "리뷰 상세 조회에 포함되는 좌석/상영/영화 DTO 입니다.")
public record ReviewSeatInfoResponse(
        @Schema(description = "영화 제목", example = "어벤져스: 엔드게임")
        String movieTitle,

        @Schema(description = "영화관 이름", example = "CGV 용산아이파크몰")
        String theaterName,

        @Schema(description = "상영관 이름", example = "IMAX관")
        String auditoriumName,

        @Schema(description = "좌석 번호", example = "F12")
        String seatNumber
) {

    public static ReviewSeatInfoResponse from(Review review) {
        Seat seat = review.getSeat();

        return ReviewSeatInfoResponse.builder()
                .movieTitle(review.getMovieTitle())
                .theaterName(seat.getAuditorium().getTheater().getName())
                .auditoriumName(seat.getAuditorium().getName())
                .seatNumber(seat.getColumn() + seat.getRow())
                .build();
    }

}
