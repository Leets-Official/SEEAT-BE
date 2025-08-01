package com.seeat.server.domain.theater.application.dto.response;

import com.seeat.server.domain.theater.domain.entity.Auditorium;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import java.util.List;

/**
 * 카테고리를 통해 선택하는 영화관 목록 DTO
 * @param auditoriumId  상영관 ID
 * @param theaterName   영화관 이름
 */

@Schema(name = "[응답][영화관] 영화관 목록 Response", description = "카테고리로 필터링한 영화관/상영관 목록 응답 DTO")
@Builder
public record TheaterListResponse(

        @Schema(description = "상영관 ID", example = "AUD12345")
        String auditoriumId,

        @Schema(description = "영화관 이름", example = "CGV 왕십리")
        String theaterName,

        @Schema(description = "상영관 이름", example = "1관")
        String auditoriumName
) {

    /// 정적 팩토리 메서드
    public static TheaterListResponse from(Auditorium auditorium) {
        return TheaterListResponse.builder()
                .auditoriumId(auditorium.getId())
                .theaterName(auditorium.getTheater().getName())
                .auditoriumName(auditorium.getName())
                .build();
    }

    public static List<TheaterListResponse> from(List<Auditorium> auditoriums) {
        return auditoriums.stream()
                .map(TheaterListResponse::from)
                .toList();
    }


}
