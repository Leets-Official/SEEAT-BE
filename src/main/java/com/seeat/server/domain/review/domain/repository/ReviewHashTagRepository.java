package com.seeat.server.domain.review.domain.repository;

import com.seeat.server.domain.review.domain.entity.Review;
import com.seeat.server.domain.review.domain.entity.ReviewHashTag;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ReviewHashTagRepository extends JpaRepository<ReviewHashTag, Long> {

    List<ReviewHashTag> findByReview_Id(Long reviewId);

    List<ReviewHashTag> findByReview_IdIn(List<Long> reviewIds);

    List<ReviewHashTag> findByReview(Review review);
}
