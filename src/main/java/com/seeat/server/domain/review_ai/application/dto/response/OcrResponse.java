package com.seeat.server.domain.review_ai.application.dto.response;

import com.seeat.server.domain.theater.domain.entity.Seat;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;

import java.util.List;

/**
 * OCR 결과를 저장하기 위한 DTO(Data Transfer Object) 클래스.
 *
 * @param theater              극장명(예: CGV 야탑)
 * @param title                영화제목(예: F1 더 무비)
 * @param auditoriumId         상영관 ID
 * @param auditoriumName       상영관(예: 2관 (Laser))
 * @param seats                좌석 정보
 */
@Builder
@Schema(name = "[응답][리뷰] 티켓 OCR Response",description = "티켓 OCR에 대한 DTO 입니다.")
public record OcrResponse(
        @Schema(description = "극장명", example = "CGV 야탑")
        String theater,

        @Schema(description = "영화제목", example = "F1 더 무비")
        String title,

        @Schema(description = "상영관 ID", example = "13084")
        String auditoriumId,

        @Schema(description = "상영관 이름", example = "2관 (Laser)")
        String auditoriumName,

        List<OcrSeatInfo> seats


) {

    /// 정적 팩토리 메서드
    public static OcrResponse from(String theater, String title, String auditoriumId, String auditoriumName, List<Seat> seats ) {
        return OcrResponse.builder()
                .theater(theater)
                .title(title)
                .auditoriumId(auditoriumId)
                .auditoriumName(auditoriumName)
                .seats(OcrSeatInfo.from(seats))
                .build();
    }

    @Builder
    record OcrSeatInfo(
            @Schema(description = "좌석 ID들", example = "13084H13")
            String seatId,

            @Schema(description = "좌석 번호", example = "H13")
            String seatName) {

        /// 정적 팩토리 메서드
        public static OcrSeatInfo from(Seat seat){
            return OcrSeatInfo.builder()
                    .seatId(seat.getId())
                    .seatName(seat.getRow() + seat.getColumn())
                    .build();
        }

        /// 정적 팩토리 메서드
        public static List<OcrSeatInfo> from(List<Seat> seats){
            return seats.stream()
                    .map(OcrSeatInfo::from)
                    .toList();
        }

    }
}
