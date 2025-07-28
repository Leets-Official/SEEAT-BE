package com.seeat.server.domain.theater.application.dto.response;

import com.seeat.server.domain.theater.domain.entity.Seat;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import java.util.List;

/**
 * 좌석 배치도를 위한 DTO 입니다.
 * @param seatId        좌석 ID
 * @param row           행
 * @param column        열
 */
@Schema(name = "좌석 배치도 DTO", description = "좌석 배치도를 위한 응답 DTO")
@Builder
public record SeatListResponse(

        @Schema(description = "좌석 ID", example = "SEAT123")
        String seatId,

        @Schema(description = "좌석 행(Row)", example = "C")
        String row,

        @Schema(description = "좌석 열(Column)", example = "12")
        String column){

    /// 정적 팩토리 메서드
    public static SeatListResponse from(Seat seat) {
        return SeatListResponse.builder()
                .seatId(seat.getId())
                .row(seat.getRow())
                .column(String.valueOf(seat.getColumn()))
                .build();
    }

    public static List<SeatListResponse> from(List<Seat> seats) {
        return seats.stream()
                .map(SeatListResponse::from)
                .toList();
    }
}
