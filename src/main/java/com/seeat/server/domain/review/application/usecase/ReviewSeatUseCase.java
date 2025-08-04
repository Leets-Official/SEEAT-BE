package com.seeat.server.domain.review.application.usecase;

import com.seeat.server.domain.review.domain.entity.Review;
import com.seeat.server.domain.theater.domain.entity.Seat;
import java.util.List;

/**
 * 리뷰와 좌석 간의 다대다 관계를 처리하기 위한 UseCase 인터페이스입니다.
 * 이 인터페이스는 리뷰와 좌석을 연결, 해제, 조회 등 다양한 비즈니스 로직을 정의하는 역할을 합니다.
 * 실 구현 클래스에서는 리뷰-좌석 매핑에 대한 등록/삭제/조회 등 상세 행위를 구현할 수 있습니다.
 */
public interface ReviewSeatUseCase {

    /**
     * 좌석과 리뷰를 연결하는 함수
     * @param seat    좌석
     * @param review  리뷰
     */
    void connectSeats(Seat seat, Review review);


    /**
     * 리뷰에 따른 좌석 목록 조회하기
     * @param review    리뷰
     */
    List<Seat> loadSeatsByReview(Review review);

    /**
     * 좌석에 따른 리뷰 조회하기
     * @param seat  좌석
     */
    List<Review> loadReviewsBySeat(Seat seat);


    /**
     * 리뷰를 해제하면 테이블에서 삭제하는 함수
     * @param review    삭제하는 리뷰
     */
    void disconnectSeats(Review review);

}
