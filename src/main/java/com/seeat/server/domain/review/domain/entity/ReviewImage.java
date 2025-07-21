package com.seeat.server.domain.review.domain.entity;

import com.seeat.server.domain.BaseEntity;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * 관람 후기 이미지 엔티티
 *
 * 후기(Review)에 첨부된 이미지 정보를 관리한다.
 * 여러 이미지가 하나의 후기에 연결될 수 있으며,
 * 이미지 순서(displayOrder)를 지정해 프론트에서 노출 순서를 제어할 수 있다.
 */

@Entity
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Getter
public class ReviewImage extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "review_id")
    private Review review;

    private String imageUrl;

    private Integer displayOrder;

    /// 정적 팩토리 메서드
    public static ReviewImage of(Review review, String imageUrl, Integer displayOrder) {
        return ReviewImage.builder()
                .review(review)
                .imageUrl(imageUrl)
                .displayOrder(displayOrder)
                .build();

    }
}
