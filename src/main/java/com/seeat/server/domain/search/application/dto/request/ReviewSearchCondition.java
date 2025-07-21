package com.seeat.server.domain.search.application.dto.request;
import java.util.List;

public class ReviewSearchCondition {
    private String keyword;           // 리뷰 내용, 영화 제목 등에 사용
    private Long theaterId;           // 특정 극장 필터링
    private Long auditoriumId;        // 상영관 필터링
    private List<Long> seatIds;       // 특정 좌석
    private List<Long> hashTagIds;    // 해시태그로 필터링
    private Integer minRating;        // 최소 평점
    private Integer maxRating;        // 최대 평점
    private String sort;              // 최신순, 평점순 등
}

