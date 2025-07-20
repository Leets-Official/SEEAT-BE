package com.seeat.server.domain.theater.application.service;

import com.seeat.server.domain.review.domain.entity.Review;
import com.seeat.server.domain.theater.application.dto.response.SeatRatingSummaryResponse;
import com.seeat.server.domain.theater.application.usecase.SeatRatingUseCase;
import com.seeat.server.domain.theater.application.usecase.TheaterUseCase;
import com.seeat.server.domain.theater.domain.entity.Auditorium;
import com.seeat.server.domain.theater.domain.entity.Seat;
import com.seeat.server.domain.theater.domain.entity.SeatRatingSummary;
import com.seeat.server.domain.theater.domain.repository.SeatRatingSummaryRepository;
import com.seeat.server.domain.theater.domain.repository.dto.SeatWithRating;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Comparator;
import java.util.List;

@Service
@Transactional
@RequiredArgsConstructor
public class SeatRatingService implements SeatRatingUseCase {

    private final SeatRatingSummaryRepository repository;

    /// 외부 의존성
    private final TheaterUseCase theaterService;

    /**
     * 특정 상영관에 대해 좌석별 평점 요약 정보를 제공합니다.
     *
     * @param auditoriumId	상영관(Auditorium) 식별자
     * @return				상영관 내 각 좌석의 평점 요약 목록
     */
    @Override
    public List<SeatRatingSummaryResponse> getSeatRatingSummariesByAuditoriumId(String auditoriumId) {

        /// 상영관 존재하는지 예외처리
        Auditorium auditorium = theaterService.getAuditorium(auditoriumId);

        /// 조회하기
        List<SeatWithRating> summaries = repository.findRatingsByAuditoriumId(auditorium.getId());

        /// 값 꺼내기
        // null이면 내부에서 0 처리됨
        return summaries.stream()
                .sorted(Comparator
                        .comparing((SeatWithRating s) -> s.getSeat().getRow())
                        .thenComparing((SeatWithRating s) -> s.getSeat().getColumn()))
                .map(summary -> SeatRatingSummaryResponse.of(
                        summary.getSeat(),
                        summary.getTotalReviews(),
                        summary.getAverageRating()
                ))
                .toList();
    }

    /**
     * 리뷰가 추가될때마다 좌석 배치도 업데이트하는 내부 함수
     * 더티 체킹을 이용한다.
     * @param review    추가된 리뷰
     * @param seat      리뷰의 좌석 정보
     */
    @Override
    public void saveSeatRating(Review review, Seat seat) {

        /// 좌석에 따른 조회
        // 기존에 존재한다면, 불러오고 없다면 새로 생성
        SeatRatingSummary summary = repository.findBySeat(seat)
                .orElse(repository.save(SeatRatingSummary.of(seat)));

        // 기존 평균 바탕으로 기존 평균 구하기
        float average = summary.getAverageGrade() * summary.getTotalReviews();

        // 기존 평균에 새로운 값 추가
        average += (float) review.getRating();

        // 새로운 개수 계산하기
        int newCount = summary.getTotalReviews() + 1;

        // 새로운 평균 계산
        float newAverage = average / newCount;

        /// 도메인 로직 사용
        summary.update(newCount, newAverage);

    }


}
