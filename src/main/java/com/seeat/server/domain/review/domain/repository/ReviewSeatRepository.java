package com.seeat.server.domain.review.domain.repository;

import com.seeat.server.domain.review.domain.entity.Review;
import com.seeat.server.domain.review.domain.entity.ReviewSeat;
import com.seeat.server.domain.theater.domain.entity.Seat;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ReviewSeatRepository extends JpaRepository<ReviewSeat, Long> {

    /**
     * 리뷰를 바탕으로 조회
     * @param review    리뷰
     */
    List<ReviewSeat> findByReview(Review review);

    /**
     * 좌석을 바탕으로 조회
     * @param seat  좌석
     */
    List<ReviewSeat> findBySeat(Seat seat);

    /**
     * 리뷰를 바탕으로 삭제
     * @param review    리뷰
     */
    void deleteByReview(Review review);

    List<ReviewSeat> findByReview_IdIn(List<Long> reviewIds);
}
