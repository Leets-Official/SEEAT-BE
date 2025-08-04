package com.seeat.server.domain.review.domain.entity;

import com.seeat.server.domain.BaseEntity;
import com.seeat.server.domain.theater.domain.entity.Seat;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * 리뷰 및 좌석 다대다 엔티티
 * 하나의 리뷰가 여러 개의 좌석에서 작성될 수 있기에 중간 테이블로 활용한다.
 */

@Entity
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Getter
public class ReviewSeat extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "review_id")
    private Review review;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "seat_id")
    private Seat seat;

    /// 정적 팩토리 메서드
    public static ReviewSeat of(Review review, Seat seat) {
        return ReviewSeat.builder()
                .review(review)
                .seat(seat)
                .build();
    }

}
