package com.seeat.server.domain.theater.application;

import com.seeat.server.domain.theater.application.dto.response.*;
import com.seeat.server.domain.theater.domain.entity.AuditoriumType;
import com.seeat.server.global.response.pageable.PageRequest;

import java.util.List;

/**
 * 영화관 관련 유즈 케이스 입니다.
 */

public interface TheaterUseCase {

    /// 영화관
    // 해당 상영관(DOLBY, IMAX)이 존재하는 영화관 조회
    // TODO! 추후 무한스크롤 구현
    List<TheaterListResponse> loadTheatersByType(AuditoriumType type, PageRequest pageRequest);

    /// 상영관
    // 상영관 상세 조회
    AuditoriumDetailResponse loadAuditorium(String auditoriumId);

    /// 좌석
    // 해당 상영관의 좌석 배치도 조회 하기
    List<SeatListResponse> loadSeatsByAuditorium(String auditoriumId);


}
