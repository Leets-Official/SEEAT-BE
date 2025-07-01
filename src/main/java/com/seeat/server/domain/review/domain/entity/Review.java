package com.seeat.server.domain.review.domain.entity;

import com.seeat.server.domain.BaseEntity;
import com.seeat.server.domain.theater.domain.entity.Seat;
import com.seeat.server.domain.user.domain.entity.User;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * 영화 리뷰 엔티티
 * - 리뷰 작성자(User), 좌석(Seat)과 연관
 * - 영화 제목, 평점, 내용, 이미지 URL 저장
 */

@Entity
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Getter
public class Review extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    private Seat seat;

    private String movieTitle;

    private double rating;

    private String content;
}
