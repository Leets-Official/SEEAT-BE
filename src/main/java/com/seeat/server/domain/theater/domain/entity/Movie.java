package com.seeat.server.domain.theater.domain.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

/**
 * 영화(Movie) 엔티티
 * - 영화 제목, 장르, 포스터 이미지 URL, 상영 시간, 개봉일을 관리
 */

@Entity
@Getter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class Movie {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String title;

    @Enumerated(EnumType.STRING)
    private MovieGenre genre;

    private String posterUrl;

    private Integer runningTime;

    private LocalDate releaseDate;
}
