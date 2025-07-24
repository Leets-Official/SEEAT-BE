package com.seeat.server.domain.theater.application.dto.response;

import com.seeat.server.domain.theater.domain.entity.Auditorium;
import com.seeat.server.domain.theater.domain.entity.AuditoriumType;
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
public record AuditoriumResponse(
        String id,
        String theaterName,
        String name,
        AuditoriumType type,
        String screenSize,
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
