package com.seeat.server.domain.theater.domain.entity;

import com.seeat.server.domain.BaseEntity;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.NoArgsConstructor;

/**
 * 상영관(Auditorium) 엔티티
 *
 * 각 영화관 내 여러 상영관 정보를 저장한다.
 * 상영관명, 형식(IMAX, Dolby 등), 스크린 크기, 음향 시스템, 좌석 수 등의 상세 정보를 관리한다.
 */

@Entity
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class Auditorium extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "theater_id")
    private Theater theater;

    private String name;

    @Enumerated(EnumType.STRING)
    private AuditoriumType type;

    private String screenSize;

    private String soundType;

    private Integer seatCount;

}
