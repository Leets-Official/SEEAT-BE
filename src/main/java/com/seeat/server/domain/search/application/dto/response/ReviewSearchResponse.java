package com.seeat.server.domain.search.application.dto.response;

import com.seeat.server.domain.review.domain.entity.HashTag;
import com.seeat.server.domain.review.domain.entity.Review;
import com.seeat.server.domain.review.domain.entity.ReviewLike;
import com.seeat.server.domain.theater.application.dto.response.TheaterListResponse;
import lombok.Builder;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

@Builder
public record ReviewSearchResponse(
        Long reviewId,
        String content,
        double rating,
        String movieTitle,
        String thumbnailUrl,
        ReviewLike reviewLike,
        List<String> hashTags
) {

    // 정적 메소드
    public static ReviewSearchResponse from(Review review, ReviewLike reviewLike, List<String> hashTags) {

        return ReviewSearchResponse.builder()
                .reviewId(review.getId())
                .content(review.getContent())
                .rating(review.getRating())
                .movieTitle(review.getMovieTitle())
                .thumbnailUrl(review.getThumbnailUrl())
                .reviewLike(reviewLike)
                .hashTags(hashTags)
                .build();
    }

    public static List<ReviewSearchResponse> from(
            List<Review> reviews,
            List<ReviewLike> reviewLikes,
            List<List<String>> hashTags) {

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
                    ReviewLike like = reviewLikeMap.get(review.getId());
                    List<String> tags = hashTagMap.getOrDefault(review.getId(), Collections.emptyList());
                    return from(review, like, tags);
                })
                .toList();
    }

}
