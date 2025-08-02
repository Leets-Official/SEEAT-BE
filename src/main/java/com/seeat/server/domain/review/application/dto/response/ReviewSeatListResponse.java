package com.seeat.server.domain.review.application.dto.response;

import com.seeat.server.domain.review.domain.repository.dto.SeatReviewStats;
import com.seeat.server.domain.theater.domain.entity.Seat;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;

import java.util.List;

/**
 * Review, 해시태그 목록, 좋아요 수를 받아 ReviewSeatListResponse로 변환합니다.
 * @param theaterName
 * @param seatName
 * @param reviewCount
 * @param averageRating
 * @param reviews
 */
@Builder
@Schema(name = "[응답][리뷰] 좌석별 리뷰 목록 조회 Response", description = "좌석별 리뷰 목록 조회에 대한 DTO 입니다.")
public record ReviewSeatListResponse(

        /// 상영관 정보
        @Schema(description = "영화관 이름", example = "남양주현대아울렛 스페이스원")
        String theaterName,

        @Schema(description = "좌석 이름", example = "G00")
        String seatName,

        @Schema(description = "리뷰 수", example = "7")
        Long reviewCount,

        @Schema(description = "평균 리뷰", example = "7")
        Double averageRating,

        /// 리스트로 처리
        List<ReviewListResponse> reviews

) {

    /// 정적 팩토리 메서드
    public static ReviewSeatListResponse from(SeatReviewStats stats, List<ReviewListResponse> reviews) {

        Seat seat = stats.getSeat();

        return ReviewSeatListResponse.builder()
                .theaterName(seat.getAuditorium().getTheater().getName())
                .seatName(seat.getRow() + seat.getColumn())
                .reviewCount(stats.getReviewCount())
                .averageRating(stats.getAverageRating())
                .reviews(reviews)
                .build();
    }

}

