package com.seeat.server.domain.review.application.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;

import java.util.List;

/**
 * OCR 결과를 저장하기 위한 DTO(Data Transfer Object) 클래스.
 *
 * @param theater              극장명(예: CGV 야탑)
 * @param title                영화제목(예: F1 더 무비)
 * @param movieType            영화 상영 종류(예: 2D, 4DX, IMAX 등)
 * @param auditoriumId         상영관 ID
 * @param auditoriumName       상영관(예: 2관 (Laser))
 * @param seatId               좌석 ID
 * @param seatName             좌석
 */
@Builder
@Schema(name = "[응답][리뷰] 티켓 OCR Response",description = "티켓 OCR에 대한 DTO 입니다.")
public record OcrResponse(
        @Schema(description = "극장명", example = "CGV 야탑")
        String theater,

        @Schema(description = "영화제목", example = "F1 더 무비")
        String title,

        @Schema(description = "영화 상영 종류", example = "IMAX")
        String movieType,

        @Schema(description = "상영관 ID", example = "13084")
        String auditoriumId,

        @Schema(description = "상영관 이름", example = "2관 (Laser)")
        String auditoriumName,

        @Schema(description = "좌석 ID", example = "13084H13")
        String seatId,

        @Schema(description = "좌석 번호", example = "H13")
        String seatName

) {

    /// 정적 팩토리 메서드
    public static OcrResponse from(String theater, String title, String movieType, String auditoriumId, String auditoriumName, String seatId, String seatName) {
        return OcrResponse.builder()
                .theater(theater)
                .title(title)
                .movieType(movieType)
                .auditoriumId(auditoriumId)
                .auditoriumName(auditoriumName)
                .seatId(seatId)
                .seatName(seatName)
                .build();
    }
}
