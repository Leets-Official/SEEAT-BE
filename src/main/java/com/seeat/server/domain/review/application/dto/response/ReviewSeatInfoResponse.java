package com.seeat.server.domain.review.application.dto.response;

import com.seeat.server.domain.theater.domain.entity.Seat;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import java.util.List;

/**
 * 관람영화 및 좌석정보 DTO
 *

 * @param seatNumber     좌석 번호 (예: "F12")
 */
@Builder
@Schema(name = "[응답][리뷰] 좌석/영화/상영관 Response", description = "리뷰 상세 조회에 포함되는 좌석/상영/영화 DTO 입니다.")
public record ReviewSeatInfoResponse(

        @Schema(description = "좌석 ID", example = "F12")
        String seatId,

        @Schema(description = "좌석 번호", example = "F12")
        String seatNumber

) {

    /// 정적 팩토리 메서드
    public static ReviewSeatInfoResponse from(Seat seat) {
        return ReviewSeatInfoResponse.builder()
                .seatId(seat.getId())
                .seatNumber(seat.getColumn() + seat.getRow())
                .build();
    }

    /// 정적 팩토리 메서드
    public static List<ReviewSeatInfoResponse> from(List<Seat> seats){
        return seats.stream()
                .map(ReviewSeatInfoResponse::from)
                .toList();
    }

}
