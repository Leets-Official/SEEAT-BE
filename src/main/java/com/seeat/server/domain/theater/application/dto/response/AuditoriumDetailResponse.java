package com.seeat.server.domain.theater.application.dto.response;

import com.seeat.server.domain.theater.domain.entity.Auditorium;
import lombok.Builder;

/**
 * 상영관 관련 상세 조회 DTO
 * @param theaterName       영화관 제목
 * @param auditoriumId      상영관 ID
 * @param auditoriumName    상영관 이름
 * @param screenSize        스크린 이름
 * @param soundType         사운드 타입
 * @param screenFormat      스크린 포맷
 */
@Builder
public record AuditoriumDetailResponse(
        String theaterName,
        String auditoriumId,
        String auditoriumName,
        String screenSize,
        String soundType,
        String screenFormat
) {

    /// 정적 팩토리 메서드
    public static AuditoriumDetailResponse from(Auditorium auditorium) {
        return AuditoriumDetailResponse.builder()
                .theaterName(auditorium.getTheater().getName())
                .auditoriumId(auditorium.getId())
                .auditoriumName(auditorium.getName())
                .screenFormat(auditorium.getScreenSize())
                .soundType(auditorium.getSoundType())
                .screenFormat("")
                .build();
    }
}
