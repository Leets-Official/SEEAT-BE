package com.seeat.server.domain.theater.domain.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * 좌석(Seat) 엔티티
 *
 * 특정 상영관(Auditorium)에 속한 개별 좌석 정보를 관리한다.
 * 좌석은 행(row)과 열(column)로 구분하며, 상영관과 다대일 관계를 가진다.
 */

@Entity
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Getter
public class Seat {

    @Id
    private String id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "auditorium_id")
    private Auditorium auditorium;

    @Column(name = "`row`")
    private String row;

    @Column(name = "`column`")
    private int column;

}
