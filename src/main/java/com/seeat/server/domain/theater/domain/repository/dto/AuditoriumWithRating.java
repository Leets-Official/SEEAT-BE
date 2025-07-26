package com.seeat.server.domain.theater.domain.repository.dto;

import com.seeat.server.domain.theater.domain.entity.Auditorium;

/**
 * 상영관과 해당 리뷰의 개수와 평점 정보를 Projection으로 조회하기 위한 인터페이스입니다.
 * Spring Data JPA에서 JPQL 쿼리 결과를 매핑하기 위해 사용됩니다.
 */
public interface AuditoriumWithRating {

    /**
     * 좌석 엔티티
     */
    Auditorium getAuditorium();

    /**
     * 해당 좌석에 대한 리뷰 개수
     */
    Integer getTotalReviews();

    /**
     * 평균 평점 (0.0 ~ 5.0 등급)
     */
    Float getAverageRating();

}
