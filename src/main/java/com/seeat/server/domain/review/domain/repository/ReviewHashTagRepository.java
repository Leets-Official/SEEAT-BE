package com.seeat.server.domain.review.domain.repository;

import com.seeat.server.domain.review.domain.entity.Review;
import com.seeat.server.domain.review.domain.entity.ReviewHashTag;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface ReviewHashTagRepository extends JpaRepository<ReviewHashTag, Long> {

    List<ReviewHashTag> findByReview_Id(Long reviewId);

    List<ReviewHashTag> findByReview_IdIn(List<Long> reviewIds);

    @Query(
            "select rht " +
            "from ReviewHashTag rht " +
            "join fetch rht.hashTag " +
            "where rht.review.id in :reviewIds"
    )
    List<ReviewHashTag> findWithHashTagByReview_Ids(@Param("reviewIds")List<Long> reviewIds);

    List<ReviewHashTag> findByReview(Review review);

    void deleteByReviewId(Long reviewId);
}
