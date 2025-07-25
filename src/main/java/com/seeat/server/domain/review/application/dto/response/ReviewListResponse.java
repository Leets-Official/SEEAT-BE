package com.seeat.server.domain.review.application.dto.response;

import com.seeat.server.domain.review.domain.entity.Review;
import com.seeat.server.domain.review.domain.entity.ReviewHashTag;
import com.seeat.server.domain.user.application.dto.response.UserResponse;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;

import java.util.List;

/**
 * Review, 해시태그 목록, 좋아요 수를 받아 ReviewListResponse로 변환합니다.
 *
 * @param reviewId      리뷰 아이디
 * @param thumbnailUrl  썸네일 이미지
 * @param hashtags      해시태그 목록 DTO
 * @param movieTitle    영화 제목
 * @param theaterName   영화관 제목
 * @param content       리뷰 내용
 * @param user          유저 관련 DTO
 * @param heartCount    좋아요(하트) 수
 */

@Builder
@Schema(name = "[응답] 리뷰 목록 조회 Response",description = "리뷰 목록 조회에 대한 DTO 입니다.")
public record ReviewListResponse(

        @Schema(description = "리뷰 아이디", example = "1")
        Long reviewId,

        @Schema(description = "썸네일 이미지 URL", example = "https://example.com/thumbnails/review101.jpg")
        String thumbnailUrl,

        List<ReviewHashTagResponse> hashtags,

        @Schema(description = "영화 제목", example = "어벤져스: 엔드게임")
        String movieTitle,

        @Schema(description = "영화관 이름", example = "CGV 용산아이파크몰")
        String theaterName,

        @Schema(description = "리뷰 내용", example = "액션이 뛰어나고 스토리가 감동적이었습니다.")
        String content,

        UserResponse user,

        @Schema(description = "좋아요(하트) 수", example = "250")
        Long heartCount
) {

    public static ReviewListResponse from(Review review, List<ReviewHashTag> hashTags, Long heartCount) {

        return ReviewListResponse.builder()
                .reviewId(review.getId())
                .thumbnailUrl(review.getThumbnailUrl())
                .hashtags(ReviewHashTagResponse
                        .from(hashTags))
                .movieTitle(review.getMovieTitle())
                .theaterName(review.getSeat().getAuditorium().getTheater().getName())
                .content(review.getContent())
                .user(UserResponse
                        .from(review.getUser()))
                .heartCount(heartCount)
                .build();
    }
}
