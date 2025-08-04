package com.seeat.server.domain.review.application.service;

import com.seeat.server.domain.review.application.usecase.ReviewSeatUseCase;
import com.seeat.server.domain.review.domain.entity.Review;
import com.seeat.server.domain.review.domain.entity.ReviewSeat;
import com.seeat.server.domain.review.domain.repository.ReviewSeatRepository;
import com.seeat.server.domain.theater.domain.entity.Seat;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;

/**
 * 리뷰와 좌석 간의 다대다 관계를 처리하는 서비스 클래스입니다.
 * - 리뷰와 좌석을 연결/해제
 * - 특정 리뷰에 연결된 좌석 조회
 * - 특정 좌석에 연결된 리뷰 조회를 지원합니다.
 */
@Service
@Transactional
@RequiredArgsConstructor
public class ReviewSeatService implements ReviewSeatUseCase {

    /// DB 의존성
    private final ReviewSeatRepository repository;

    /**
     * 특정 좌석과 리뷰를 연결(매핑)합니다.
     *
     * @param seat   좌석 엔티티
     * @param review 리뷰 엔티티
     */
    @Override
    public void connectSeats(Seat seat, Review review) {

        /// 객체 생성
        ReviewSeat reviewSeat = ReviewSeat.of(review, seat);

        /// DB에 저장
        repository.save(reviewSeat);

    }

    /**
     * 주어진 리뷰에 연결된 모든 좌석 목록을 조회합니다.
     *
     * @param review 리뷰 엔티티
     * @return 좌석 엔티티 목록
     */
    @Override
    public List<Seat> loadSeatsByReview(Review review) {

        /// 해당 리뷰의 다대다 테이블에서 조회하기
        List<ReviewSeat> seats = repository.findByReview(review);

        /// 좌석만 추출하기
        return seats.stream()
                .map(ReviewSeat::getSeat)
                .toList();
    }

    /**
     * 주어진 좌석에 연결된 모든 리뷰 목록을 조회합니다.
     *
     * @param seat 좌석 엔티티
     * @return 리뷰 엔티티 목록
     */
    @Override
    public List<Review> loadReviewsBySeat(Seat seat) {
        /// 해당 리뷰의 다대다 테이블에서 조회하기
        List<ReviewSeat> seats = repository.findBySeat(seat);

        /// 리뷰만 추출하기
        return seats.stream()
                .map(ReviewSeat::getReview)
                .toList();

    }

    @Override
    public Map<Long, List<Seat>> loadSeatsByReviewIds(List<Long> reviewIds) {
        List<ReviewSeat> result = repository.findByReview_IdIn(reviewIds);

        return result.stream()
                .collect(
                        java.util.stream.Collectors.groupingBy(
                                rs -> rs.getReview().getId(),
                                java.util.stream.Collectors.mapping(
                                        ReviewSeat::getSeat,
                                        java.util.stream.Collectors.toList()
                                )
                        )
                );
    }

    /**
     * 특정 리뷰에 연결된 모든 좌석 매핑을 해제(삭제)합니다.
     *
     * @param reviewId 삭제할 리뷰
     */
    @Override
    public void disconnectSeats(Long reviewId) {

        /// 해당 좌석에 존재하는 행 전부 삭제하기
        repository.deleteByReviewId(reviewId);

    }

}
