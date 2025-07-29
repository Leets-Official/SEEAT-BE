package com.seeat.server.domain.theater.application.dto.response;

import com.seeat.server.domain.theater.domain.entity.Auditorium;
import com.seeat.server.domain.theater.domain.entity.AuditoriumType;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;

/**
 * 상영관 정보 조회 응답 DTO
 * @param id 상영관 Id
 * @param theaterName 영화관 이름
 * @param name 상영관 이름
 * @param type 상영관 종류
 * @param screenSize 상영관 스크린 사이즈
 * @param soundType 상영관 사운드 타입
 */
@Builder
@Schema(name = "[응답][유저] 상영관 상세 조회 Response",description = "유저에서 사용하는 관심 상영관 조회에 대한 DTO 입니다.")
public record AuditoriumResponse(

        @Schema(description = "상영관 ID", example = "13018")
        String id,

        @Schema(description = "영화관 이름", example = "메가박스 강남")
        String theaterName,

        @Schema(description = "상영관 이름", example = "1관 IMAX")
        String name,

        @Schema(description = "상영관 종류")
        AuditoriumType type,

        @Schema(description = "스크린 크기 또는 타입", example = "IMAX Laser")
        String screenSize,

        @Schema(description = "사운드 타입", example = "Dolby Atmos")
        String soundType

) {
    /// 정적 팩토리 메서드
    public static AuditoriumResponse from(Auditorium auditorium) {
        return AuditoriumResponse.builder()
                .id(auditorium.getId())
                .theaterName(auditorium.getTheater() != null ? auditorium.getTheater().getName() : null)
                .name(auditorium.getName())
                .type(auditorium.getType())
                .screenSize(auditorium.getScreenSize())
                .soundType(auditorium.getSoundType())
                .build();
    }
}
