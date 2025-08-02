package com.seeat.server.domain.review.domain.repository;

import com.seeat.server.domain.review.domain.entity.Review;
import com.seeat.server.domain.review.domain.entity.ReviewLike;
import com.seeat.server.domain.review.domain.repository.dto.ReviewWithLikeCount;
import com.seeat.server.domain.user.domain.entity.User;
import io.lettuce.core.dynamic.annotation.Param;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface ReviewLikeRepository extends JpaRepository<ReviewLike, Long> {

    boolean existsByUserAndReview(User user, Review review);

    void deleteByUserAndReview(User user, Review review);

    List<ReviewLike> findAllByUserAndReviewIn(User user, List<Review> reviews);

    @Query(
            "SELECT rl.review AS review, COUNT(rl) AS likeCount " +
                    "FROM ReviewLike rl " +
                    "WHERE rl.review IN :reviews " +
                    "GROUP BY rl.review"
    )
    List<ReviewWithLikeCount> countLikesByReviewIn(@Param("reviews") List<Review> reviews);
}
