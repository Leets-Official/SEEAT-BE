package com.seeat.server.domain.review.domain.entity;

import com.seeat.server.domain.BaseEntity;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * 리뷰와 해시태그 간의 다대다 관계를 위한 중간 엔티티
 * - 하나의 리뷰에 여러 해시태그가 붙고,
 * - 하나의 해시태그는 여러 리뷰에 사용될 수 있음
 */

@Entity
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Getter
public class ReviewHashTag extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "hashtag_id")
    private HashTag hashTag;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "review_id")
    private Review review;
}
