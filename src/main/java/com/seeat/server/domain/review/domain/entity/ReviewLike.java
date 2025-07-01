package com.seeat.server.domain.review.domain.entity;

import com.seeat.server.domain.BaseEntity;
import com.seeat.server.domain.user.domain.entity.User;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * 리뷰 좋아요(Like) 엔티티
 * - 특정 유저가 특정 리뷰에 대해 좋아요를 표시한 기록을 나타냄
 */

@Entity
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Getter
public class ReviewLike extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    private Review review;
}
