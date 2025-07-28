package com.seeat.server.domain.review.application.dto.response;

import com.seeat.server.domain.review.domain.entity.Review;
import com.seeat.server.domain.review.domain.entity.ReviewHashTag;
import com.seeat.server.domain.user.application.dto.response.UserResponse;
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
public record ReviewListResponse(
        Long reviewId,
        String thumbnailUrl,
        List<ReviewHashTagResponse> hashtags,
        String movieTitle,
        String theaterName,
        String content,
        UserResponse user,
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
