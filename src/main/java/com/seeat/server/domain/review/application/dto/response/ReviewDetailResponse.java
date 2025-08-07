package com.seeat.server.domain.review.application.dto.response;

import com.seeat.server.domain.hashtag.application.dto.response.ReviewHashTagResponse;
import com.seeat.server.domain.review.domain.entity.Review;
import com.seeat.server.domain.hashtag.domain.entity.ReviewHashTag;
import com.seeat.server.domain.image.domain.entity.ReviewImage;
import com.seeat.server.domain.theater.domain.entity.Seat;
import com.seeat.server.domain.user.application.dto.response.UserResponse;
import com.seeat.server.domain.image.application.dto.response.ReviewImageInfoResponse;
import com.seeat.server.global.util.DateFormatUtil;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;

import java.util.List;

/**
 * 리뷰 상세조회 응답 DTO
 * @param movieTitle        영화 제목
 * @param auditoriumName    상영관 이름
 * @param seatInfo          좌석 정보 DTO
 * @param hashtags          해시태그 목록 DTO
 * @param title             후기 제목
 * @param content           리뷰 내용
 * @param rating            평점 (1~5)
 * @param user              작성자 정보 DTO
 * @param heartCount        좋아요 개수
 * @param imageInfo         이미지 목록 DTO
 * @param createdAt         리뷰 작성 시간
 */

@Builder
@Schema(name = "[응답][리뷰] 리뷰 상세 조회 Response",description = "리뷰 상세 조회에 대한 DTO 입니다.")
public record ReviewDetailResponse(

        @Schema(description = "영화 제목", example = "어벤져스: 엔드게임")
        String movieTitle,

        @Schema(description = "상영관 ID", example = "13083")
        String auditoriumId,

        @Schema(description = "상영관 이름", example = "CGV 용산아이파크몰 IMAX관")
        String auditoriumName,

        List<ReviewSeatInfoResponse> seatInfo,

        List<ReviewHashTagResponse> hashtags,

        @Schema(description = "리뷰 제목", example = "용아맥은 전설이다!")
        String title,

        @Schema(description = "리뷰 내용", example = "정말 재미있고 감동적이었어요!")
        String content,

        @Schema(description = "평점 (1~5)", example = "4.5")
        double rating,

        UserResponse user,

        List<ReviewImageInfoResponse> imageInfo,

        @Schema(description = "좋아요 여부", example = "3.7")
        boolean liked,

        @Schema(description = "좋아요 개수", example = "152")
        Long heartCount,

        @Schema(description = "리뷰 작성 시간", example = "2025-07-24T14:35:00")
        String createdAt
) {

    public static ReviewDetailResponse from(
            Review review,
            List<ReviewHashTag> hashTags, Long heartCount, List<ReviewImage> images, List<Seat> seats, boolean liked
    ) {

        /// 같은 상영관의 좌석이기에,
        Seat seat = seats.get(0);

        return ReviewDetailResponse.builder()
                .movieTitle(review.getMovieTitle())
                .auditoriumId(seat.getAuditorium().getId())
                .auditoriumName(seat.getAuditorium().getTheater().getName() + " " + seat.getAuditorium().getName())
                .seatInfo(ReviewSeatInfoResponse
                        .from(seats))
                .hashtags(ReviewHashTagResponse
                        .from(hashTags))
                .title(review.getTitle())
                .content(review.getContent())
                .rating(review.getRating())
                .heartCount(heartCount)
                .liked(liked)
                .user(UserResponse
                        .from(review.getUser()))
                .imageInfo(ReviewImageInfoResponse.from(images))
                .createdAt(DateFormatUtil.formatDate(review.getCreatedAt()))
                .build();
    }
}
