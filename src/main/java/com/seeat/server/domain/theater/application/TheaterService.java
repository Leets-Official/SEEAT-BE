package com.seeat.server.domain.theater.application;

import com.seeat.server.domain.theater.application.dto.response.*;
import com.seeat.server.domain.theater.domain.entity.Auditorium;
import com.seeat.server.domain.theater.domain.entity.AuditoriumType;
import com.seeat.server.domain.theater.domain.entity.Seat;
import com.seeat.server.domain.theater.domain.repository.AuditoriumRepository;
import com.seeat.server.domain.theater.domain.repository.SeatRepository;
import com.seeat.server.global.response.ErrorCode;
import com.seeat.server.global.response.pageable.PageRequest;
import com.seeat.server.global.response.pageable.PageUtil;
import com.seeat.server.global.response.pageable.SliceResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Slice;
import org.springframework.data.domain.SliceImpl;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Comparator;
import java.util.List;
import java.util.NoSuchElementException;

/**
 * 영화관/상영관/좌석 관련 서비스
 */
@Service
@Transactional
@RequiredArgsConstructor
public class TheaterService implements TheaterUseCase{

    private final AuditoriumRepository auditoriumRepository;
    public final SeatRepository seatRepository;

    /**
     * 해당 상영관이 존재하는 영화관 목록 조회
     * @param type  영화관 타입
     */
    @Override
    public SliceResponse<TheaterListResponse> loadTheatersByType(AuditoriumType type, PageRequest pageRequest) {

        /// 페이징 처리
        org.springframework.data.domain.PageRequest pageable = PageUtil.getPageable(pageRequest);

        /// 상영관이 존재하는 영화관 조회
        Slice<Auditorium> auditoriums = auditoriumRepository.findByType(type,pageable);

        /// DTO 변환
        List<Auditorium> content = auditoriums.getContent();
        List<TheaterListResponse> responses = TheaterListResponse.from(content);

        SliceImpl<TheaterListResponse> slice = new SliceImpl<>(responses, auditoriums.getPageable(), auditoriums.hasNext());

        return SliceResponse.from(slice);
    }


    /**
     * 아이디 바탕으로 상영관 상세 조회
     * @param auditoriumId  상영관 ID
     */
    @Override
    public AuditoriumDetailResponse loadAuditorium(String auditoriumId) {

        /// 상영관 예외처리
        Auditorium auditorium = getAuditorium(auditoriumId);

        /// DTO 변환
        return AuditoriumDetailResponse.from(auditorium);
    }

    /**
     * 해당 상영관의 좌석 배치도 조회 하기
     * @param auditoriumId  상영관 ID
     */
    @Override
    public List<SeatListResponse> loadSeatsByAuditorium(String auditoriumId) {

        /// 상영관 예외처리
        Auditorium auditorium = getAuditorium(auditoriumId);

        /// 가져오기
        List<Seat> seats = seatRepository.findByAuditorium(auditorium);

        /// 정렬
        seats.sort(Comparator
                .comparing(Seat::getRow)
                .thenComparingInt(Seat::getColumn));

        return SeatListResponse.from(seats);
    }


    /**
     * 공통 응답 함수
     * @param auditoriumId  상영관 ID
     */
    private Auditorium getAuditorium(String auditoriumId) {
        return auditoriumRepository.findById(auditoriumId)
                .orElseThrow(() -> new NoSuchElementException(ErrorCode.NOT_AUDITORIUM.getMessage()));
    }

}
