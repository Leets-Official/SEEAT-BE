package com.seeat.server.domain.review.domain.entity;

import com.seeat.server.domain.BaseEntity;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * 해시태그 엔티티
 * - 리뷰에 붙는 태그 정보를 저장
 */

@Entity
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Getter
public class HashTag extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name;

    @Enumerated(EnumType.STRING)
    private HashTagType type;

}
