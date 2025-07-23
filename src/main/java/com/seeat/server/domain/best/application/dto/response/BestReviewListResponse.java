package com.seeat.server.domain.best.application.dto.response;

import com.seeat.server.domain.best.domain.entity.BestReviewSnapshot;
import com.seeat.server.domain.review.domain.entity.HashTag;
import com.seeat.server.domain.review.domain.entity.Review;
import com.seeat.server.domain.review.domain.entity.ReviewHashTag;
import com.seeat.server.domain.user.domain.entity.User;
import lombok.Builder;

import java.util.List;

/**
 * Review, 해시태그 목록, 좋아요 수를 받아 ReviewListResponse로 변환합니다.
 *
 * @param reviewId          리뷰 아이디
 * @param thumbnailUrl      썸네일 이미지
 * @param hashtags          해시태그 목록 DTO
 * @param movieTitle        영화 제목
 * @param theaterName       영화관 제목
 * @param content           리뷰 내용
 * @param userId            유저 ID
 * @param nickname          닉네임
 * @param profileImageUrl   프로필 이미지
 * @param heartCount        좋아요(하트) 수
 */

@Builder
public record BestReviewListResponse(
        Long reviewId,
        String thumbnailUrl,
        List<String> hashtags,
        String movieTitle,
        String theaterName,
        String content,
        Long userId,
        String nickname,
        String profileImageUrl,
        Long heartCount
) {

    /// 정적 팩토리 메서드
    public static BestReviewListResponse from(BestReviewSnapshot snapshot) {

        return BestReviewListResponse.builder()
                .reviewId(snapshot.getReviewId())
                .thumbnailUrl(snapshot.getThumbnailUrl())
                .hashtags(snapshot.getHashTags())
                .movieTitle(snapshot.getMovieTitle())
                .theaterName(snapshot.getTheaterName())
                .content(snapshot.getContent())
                .userId(snapshot.getUserId())
                .nickname(snapshot.getNickname())
                .profileImageUrl(snapshot.getProfileImageUrl())
                .heartCount(snapshot.getHeartCount())
                .build();
    }

    /// 정적 팩토리 메서드
    public static List<BestReviewListResponse> from(List<BestReviewSnapshot> snapshots) {
        return snapshots.stream()
                .map(BestReviewListResponse::from)
                .toList();
    }

    /// 정적 팩토리 메서드
    public static BestReviewListResponse from(Review review, List<ReviewHashTag> hashTags, Long heartCount) {

        /// 해시태그들 정리
        List<String> hashtags = hashTags.stream()
                .map(ReviewHashTag::getHashTag)
                .map(HashTag::getName)
                .toList();

        /// 유저
        User user = review.getUser();

        return BestReviewListResponse.builder()
                .reviewId(review.getId())
                .thumbnailUrl(review.getThumbnailUrl())
                .hashtags(hashtags)
                .movieTitle(review.getMovieTitle())
                .theaterName(review.getSeat().getAuditorium().getTheater().getName())
                .content(review.getContent())
                .userId(user.getId())
                .nickname(user.getNickname())
                .profileImageUrl(user.getImageUrl())
                .heartCount(heartCount)
                .build();
    }

}
