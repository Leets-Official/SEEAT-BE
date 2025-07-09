package com.seeat.server.domain.review.domain;

import com.seeat.server.domain.review.domain.entity.Review;
import com.seeat.server.domain.theater.domain.entity.Seat;
import com.seeat.server.domain.user.domain.entity.User;

/**
 * [ 리뷰 도메인을 생성해두는 Fixtures ]입니다.
 * - 테스트에 사용할 도메인들을 미리 정의해두어, 테스트의 가독성을 높여주도록 수행합니다.
 */

public class ReviewFixtures {

    public static Review fakeReview(User user, Seat seat) {
        return Review.builder()
                .id(1L)
                .content("testContent1")
                .movieTitle("testTitle1")
                .rating(1)
                .user(user)
                .seat(seat)
                .build();
    }

    public static Review createReview(User user, Seat seat) {
        return Review.builder()
                .content("testContent1")
                .movieTitle("testTitle1")
                .rating(1)
                .user(user)
                .seat(seat)
                .build();
    }

    public static Review createReview(User user, Seat seat, int rating) {
        return Review.builder()
                .content("testContent2")
                .movieTitle("testTitle2")
                .rating(rating)
                .user(user)
                .seat(seat)
                .build();
    }

    public static Review createReview(User user, Seat seat, int rating, String content) {
        return Review.builder()
                .content(content)
                .movieTitle("testTitle2")
                .rating(rating)
                .user(user)
                .seat(seat)
                .build();
    }

    public static Review createReview(User user, Seat seat, int rating, String content, String title) {
        return Review.builder()
                .content(content)
                .movieTitle(title)
                .rating(rating)
                .user(user)
                .seat(seat)
                .build();
    }

}
