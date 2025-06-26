package com.seeat.server.domain.review.domain.entity;


import com.seeat.server.domain.BaseEntity;
import com.seeat.server.domain.theater.domain.entity.Seat;
import com.seeat.server.domain.user.domain.entity.User;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.NoArgsConstructor;

/**
 * 유저가 찜(북마크)한 좌석 정보를 나타내는 엔티티
 * - 한 유저가 여러 좌석을 찜할 수 있음
 * - 좌석에 대한 추가 설명(description) 필드 포함
 */

@Entity
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class Bookmark extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    private Seat seat;

    private String description;

}
