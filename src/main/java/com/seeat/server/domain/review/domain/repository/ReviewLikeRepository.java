package com.seeat.server.domain.review.domain.repository;

import com.seeat.server.domain.review.domain.entity.Review;
import com.seeat.server.domain.review.domain.entity.ReviewLike;
import com.seeat.server.domain.user.domain.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ReviewLikeRepository extends JpaRepository<ReviewLike, Long> {

    boolean existsByUserAndReview(User user, Review review);

    void deleteByUserAndReview(User user, Review review);

    List<ReviewLike> findAllByUserAndReviewIn(User user, List<Review> reviewList);
}
