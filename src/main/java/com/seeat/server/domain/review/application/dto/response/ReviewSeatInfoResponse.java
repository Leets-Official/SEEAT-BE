package com.seeat.server.domain.review.application.dto.response;

import com.seeat.server.domain.review.domain.entity.Review;
import com.seeat.server.domain.theater.domain.entity.Seat;
import lombok.Builder;

/**
 * 관람영화 및 좌석정보 DTO
 *
 * @param movieTitle      영화 제목
 * @param theaterName     영화관 이름
 * @param auditoriumName  상영관 이름
 * @param seatNumber      좌석 번호 (예: "F12")
 */
@Builder
public record ReviewSeatInfoResponse(
        String movieTitle,
        String theaterName,
        String auditoriumName,
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
