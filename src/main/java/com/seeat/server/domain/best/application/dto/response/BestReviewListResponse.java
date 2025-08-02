package com.seeat.server.domain.best.application.dto.response;

import com.seeat.server.domain.best.domain.entity.BestReviewSnapshot;
import com.seeat.server.domain.review.domain.entity.HashTag;
import com.seeat.server.domain.review.domain.entity.Review;
import com.seeat.server.domain.review.domain.entity.ReviewHashTag;
import com.seeat.server.domain.user.domain.entity.User;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;

import java.time.LocalDateTime;
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
 * @param originalCreatedAt 원본 게시글의 작성시간
 * @param createdAt         스냅샷 시간
 */
@Builder
@Schema(name = "[응답][베스트] 인기 리뷰 리스트 Response", description = "인기 리뷰 리스트 조회를 위한 DTO입니다.")
public record BestReviewListResponse(
        @Schema(description = "리뷰 아이디", example = "101")
        Long reviewId,

        @Schema(description = "썸네일 이미지 URL", example = "https://example.com/thumbnails/review101.jpg")
        String thumbnailUrl,

        @Schema(description = "해시태그 목록", example = "[\"감동\", \"추천\", \"액션\"]")
        List<String> hashtags,

        @Schema(description = "영화 제목", example = "어벤져스: 엔드게임")
        String movieTitle,

        @Schema(description = "영화관 이름", example = "CGV 용산아이파크몰")
        String theaterName,

        @Schema(description = "리뷰 제목", example = "용아맥은 전설이다!")
        String title,

        @Schema(description = "리뷰 내용", example = "스토리가 아주 감동적이었어요!")
        String content,

        @Schema(description = "유저 ID", example = "12345")
        Long userId,

        @Schema(description = "닉네임", example = "happyUser")
        String nickname,

        @Schema(description = "프로필 이미지 URL", example = "https://example.com/profiles/happyUser.jpg")
        String profileImageUrl,

        @Schema(description = "좋아요(하트) 수", example = "350")
        Long heartCount,

        @Schema(description = "원본 게시글의 작성 시간", example = "2025-07-23T10:15:30")
        LocalDateTime originalCreatedAt,

        @Schema(description = "스냅샷 시간", example = "2025-07-24T08:00:00")
        LocalDateTime createdAt
) {

    /// 정적 팩토리 메서드
    public static BestReviewListResponse from(BestReviewSnapshot snapshot) {

        return BestReviewListResponse.builder()
                .reviewId(snapshot.getReviewId())
                .thumbnailUrl(snapshot.getThumbnailUrl())
                .hashtags(snapshot.getHashTags())
                .movieTitle(snapshot.getMovieTitle())
                .theaterName(snapshot.getTheaterName())
                .title(snapshot.getTitle())
                .content(snapshot.getContent())
                .userId(snapshot.getUserId())
                .nickname(snapshot.getNickname())
                .profileImageUrl(snapshot.getProfileImageUrl())
                .heartCount(snapshot.getHeartCount())
                .originalCreatedAt(snapshot.getOriginalCreatedAt())
                .createdAt(snapshot.getCreatedAt())
                .build();
    }

    /// 정적 팩토리 메서드
    public static List<BestReviewListResponse> from(List<BestReviewSnapshot> snapshots) {
        return snapshots.stream()
                .map(BestReviewListResponse::from)
                .toList();
    }

    /// DB에서 직접 조회 정적 팩토리 메서드
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
                .title(review.getTitle())
                .content(review.getContent())
                .userId(user.getId())
                .nickname(user.getNickname())
                .profileImageUrl(user.getImageUrl())
                .heartCount(heartCount)
                .originalCreatedAt(review.getCreatedAt())
                .createdAt(LocalDateTime.now())
                .build();
    }

}
