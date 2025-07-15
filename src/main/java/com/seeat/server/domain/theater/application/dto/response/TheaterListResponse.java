package com.seeat.server.domain.theater.application.dto.response;

import com.seeat.server.domain.theater.domain.entity.Auditorium;
import lombok.Builder;
import java.util.List;

/**
 * 카테고리를 통해 선택하는 영화관 목록 DTO
 * @param auditoriumId  상영관 ID
 * @param theaterName   영화관 이름
 */

@Builder
public record TheaterListResponse(
        String auditoriumId,
        String theaterName) {

    /// 정적 팩토리 메서드
    public static TheaterListResponse from(Auditorium auditorium) {
        return TheaterListResponse.builder()
                .auditoriumId(auditorium.getId())
                .theaterName(auditorium.getTheater().getName())
                .build();
    }

    public static List<TheaterListResponse> from(List<Auditorium> auditoriums) {
        return auditoriums.stream()
                .map(TheaterListResponse::from)
                .toList();
    }


}
