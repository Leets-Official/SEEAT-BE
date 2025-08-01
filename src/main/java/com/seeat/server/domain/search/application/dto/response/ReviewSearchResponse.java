package com.seeat.server.domain.search.application.dto.response;

import com.seeat.server.domain.review.domain.entity.HashTag;
import com.seeat.server.domain.review.domain.entity.Review;
import com.seeat.server.domain.review.domain.entity.ReviewLike;
import com.seeat.server.domain.theater.application.dto.response.TheaterListResponse;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

@Builder
@Schema(name = "[응답][검색] 검색을 통한 리뷰 조회 Response",description = "검색을 통한 리뷰 조회에 대한 DTO 입니다.")
public record ReviewSearchResponse(

        @Schema(description = "리뷰 고유 ID", example = "12")
        Long reviewId,
        @Schema(description = "리뷰 내용", example = "화질이 다른 곳보다 선명했어요")
        String content,
        @Schema(description = "평점", example = "3.4")
        double rating,
        @Schema(description = "리뷰 제목", example = "괴물")
        String movieTitle,
        @Schema(description = "리뷰 이미지")
        String thumbnailUrl,
        @Schema(description = "좋아요 수", example = "3")
        Long likeCount,
        @Schema(description = "유저 좋아요 여부", example = "true")
        Boolean likedByUser,
        @Schema(description = "해시태그", example = "[\"혼자서\", \"사운드 빵빵\",\"화질 선명\"]")
        List<String> hashTags
) {

    // 정적 메소드
    public static ReviewSearchResponse from(Review review, Long likeCount,
                                            Boolean likedByUser, List<String> hashTags) {

        return ReviewSearchResponse.builder()
                .reviewId(review.getId())
                .content(review.getContent())
                .rating(review.getRating())
                .movieTitle(review.getMovieTitle())
                .thumbnailUrl(review.getThumbnailUrl())
                .likeCount(likeCount != null ? likeCount : 0L)
                .likedByUser(likedByUser != null ? likedByUser : false)
                .hashTags(hashTags)
                .build();
    }

    public static List<ReviewSearchResponse> from(List<Review> reviews, List<ReviewLike> reviewLikes,
                                                  Map<Long, Long> likeCountMap, List<List<String>> hashTags) {

        Map<Long, ReviewLike> reviewLikeMap = reviewLikes.stream()
                .collect(Collectors.toMap(
                        rl -> rl.getReview().getId(),
                        rl -> rl
                ));

        Map<Long, List<String>> hashTagMap = IntStream.range(0, reviews.size())
                .boxed()
                .collect(Collectors.toMap(
                        i -> reviews.get(i).getId(),
                        i -> hashTags.get(i)
                ));

        return reviews.stream()
                .map(review -> {
                    ReviewLike userLike = reviewLikeMap.get(review.getId());
                    List<String> tags = hashTagMap.getOrDefault(review.getId(), Collections.emptyList());

                    Long likeCount = likeCountMap.getOrDefault(review.getId(), 0L);
                    Boolean likedByUser = userLike != null;

                    return from(review, likeCount, likedByUser, tags);
                })
                .toList();
    }

}
