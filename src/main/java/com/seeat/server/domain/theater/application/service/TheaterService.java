package com.seeat.server.domain.theater.application.service;

import com.seeat.server.domain.theater.application.usecase.TheaterUseCase;
import com.seeat.server.domain.best.application.dto.response.BestAuditoriumListResponse;
import com.seeat.server.domain.theater.application.dto.response.*;
import com.seeat.server.domain.theater.domain.entity.Auditorium;
import com.seeat.server.domain.theater.domain.entity.AuditoriumType;
import com.seeat.server.domain.theater.domain.entity.Seat;
import com.seeat.server.domain.theater.domain.entity.Theater;
import com.seeat.server.domain.theater.domain.repository.AuditoriumRepository;
import com.seeat.server.domain.theater.domain.repository.SeatRepository;
import com.seeat.server.domain.theater.domain.repository.TheaterRepository;
import com.seeat.server.domain.theater.domain.repository.dto.AuditoriumWithScore;
import com.seeat.server.domain.theater.domain.repository.dto.AuditoriumWithRating;
import com.seeat.server.global.response.ErrorCode;
import com.seeat.server.global.response.pageable.PageRequest;
import com.seeat.server.global.response.pageable.PageUtil;
import com.seeat.server.global.response.pageable.SliceResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Slice;
import org.springframework.data.domain.SliceImpl;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import static com.seeat.server.global.response.pageable.PageUtil.getPageable;

/**
 * 영화관/상영관/좌석 관련 서비스
 */
@Service
@Transactional
@RequiredArgsConstructor
public class TheaterService implements TheaterUseCase {

    private final TheaterRepository theaterRepository;
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

        /// 상영관 예외처리 및 개수 및 평균 평점 가져오기
        AuditoriumWithRating auditorium = auditoriumRepository.findAuditoriumWithRating(auditoriumId)
                .orElseThrow(() -> new NoSuchElementException(ErrorCode.NOT_AUDITORIUM.getMessage()));

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
    public Auditorium getAuditorium(String auditoriumId) {
        return auditoriumRepository.findById(auditoriumId)
                .orElseThrow(() -> new NoSuchElementException(ErrorCode.NOT_AUDITORIUM.getMessage()));
    }

    /**
     * 공통 응답 함수
     * @param seatId  좌석 ID
     */
    @Override
    public Seat getSeat(String seatId) {
        return seatRepository.findById(seatId)
                .orElseThrow(() -> new NoSuchElementException(ErrorCode.NOT_SEAT.getMessage()));
    }

    /**
     * 공통 응답 함수
     * @param seatIds  좌석 IDs
     */
    @Override
    public List<Seat> getSeat(List<String> seatIds) {

        /// 가져오기
        List<Seat> seats = seatRepository.findByIdIn(seatIds);

        /// 예외처리
        if (seats.size() != seatIds.size()) {
            throw new NoSuchElementException(ErrorCode.NOT_SEAT.getMessage());
        }

        return seats;
    }

    @Override
    public Seat getSeatByName(String theaterName, String auditoriumName, String seatName) {

        /// 영화관 예외 처리
        Theater theater = theaterRepository.findByName(theaterName)
                .orElseThrow(() -> new NoSuchElementException(ErrorCode.NOT_THEATER.getMessage()));


        /// 상영관 예외 처리
        Auditorium auditorium = auditoriumRepository.findByNameAndTheater_Id(auditoriumName, theater.getId())
                .orElseThrow(() -> new NoSuchElementException(ErrorCode.NOT_AUDITORIUM.getMessage()));

        /// 좌석 행과 열 분리
        String row = seatName.substring(0, 1);
        int column = Integer.parseInt(seatName.substring(1));


        /// 좌석 예외 처리
        return seatRepository.findByRowAndColumnAndAuditorium_Id(row, column, auditorium.getId())
                .orElseThrow(() -> new NoSuchElementException(ErrorCode.NOT_SEAT.getMessage()));
    }


    /**
     * 외부 참조 함수
     */
    // 베스트 상영관 가져오기
    @Override
    public SliceResponse<BestAuditoriumListResponse> loadBestAuditoriums(PageRequest pageRequest) {

        /// Pageable 가져오기
        org.springframework.data.domain.PageRequest pageable = getPageable(pageRequest);

        /// 서비스 조회
        Slice<AuditoriumWithScore> slice = auditoriumRepository.findBestAuditoriums(pageable);

        if (!slice.hasContent()) {

            Slice<BestAuditoriumListResponse> emptySlice = new SliceImpl<>(Collections.emptyList(), pageable, false);

            return SliceResponse.from(emptySlice);
        }

        /// DTO 변경
        Slice<BestAuditoriumListResponse> sliceResponse = BestAuditoriumListResponse.from(slice);

        return SliceResponse.from(sliceResponse);
    }

}
