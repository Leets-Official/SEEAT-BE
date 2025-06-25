package com.seeat.server.domain.user.domain.entity;

import com.seeat.server.domain.BaseEntity;
import com.seeat.server.domain.theater.domain.entity.Theater;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.NoArgsConstructor;

/**
 * 유저와 극장 간의 선호 관계를 나타내는 중간 엔티티
 * - 다대다 관계에 추가 정보를 부여하기 위해 설계됨
 * - 현재는 대표 극장 여부(isMainTheater)를 포함
 */

@Entity
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class UserTheater extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "theater_id")
    private Theater theater;

    private boolean isMainTheater;
}
