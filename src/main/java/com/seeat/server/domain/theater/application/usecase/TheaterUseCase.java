package com.seeat.server.domain.theater.application.usecase;

import com.seeat.server.domain.best.application.dto.response.BestAuditoriumListResponse;
import com.seeat.server.domain.theater.application.dto.response.*;
import com.seeat.server.domain.theater.domain.entity.Auditorium;
import com.seeat.server.domain.theater.domain.entity.AuditoriumType;
import com.seeat.server.domain.theater.domain.entity.Seat;
import com.seeat.server.global.response.pageable.PageRequest;
import com.seeat.server.global.response.pageable.SliceResponse;

import java.util.List;

/**
 * 영화관 관련 유즈 케이스 입니다.
 */

public interface TheaterUseCase {

    /// 영화관
    // 해당 상영관(DOLBY, IMAX)이 존재하는 영화관 조회
    SliceResponse<TheaterListResponse> loadTheatersByType(AuditoriumType type, PageRequest pageRequest);

    /// 상영관
    // 상영관 상세 조회
    AuditoriumDetailResponse loadAuditorium(String auditoriumId);

    /// 좌석
    // 해당 상영관의 좌석 배치도 조회 하기
    List<SeatListResponse> loadSeatsByAuditorium(String auditoriumId);


    /// 외부 의존성
    // 예외처리를 위한
    Auditorium getAuditorium(String auditoriumId);

    Seat getSeat(String seatId);
    /// 외부 의존성
    // 베스트 상영관 가져오기
    SliceResponse<BestAuditoriumListResponse> loadBestAuditoriums(PageRequest pageRequest);



    List<Seat> getSeat(List<String> seatIds);
}
