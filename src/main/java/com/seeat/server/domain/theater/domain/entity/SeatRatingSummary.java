package com.seeat.server.domain.theater.domain.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 좌석 평점 요약 엔티티
 * - 특정 좌석에 대한 전체 리뷰 수, 평균 평점, 마지막 업데이트 시각을 저장
 */

@Entity
@Getter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class SeatRatingSummary {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "seat_id")
    private Seat seat;

    private Integer totalReviews;

    private Float averageGrade;

    private LocalDateTime lastUpdated;
}
