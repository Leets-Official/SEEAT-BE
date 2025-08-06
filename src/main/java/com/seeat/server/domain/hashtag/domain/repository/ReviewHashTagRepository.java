package com.seeat.server.domain.hashtag.domain.repository;

import com.seeat.server.domain.review.domain.entity.Review;
import com.seeat.server.domain.hashtag.domain.entity.ReviewHashTag;
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


    /**
     * 상영관 기반 해시태그 검색
     *
     * @param auditoriumId 상영관
     * @return List<Review>
     */
    @Query("""
    SELECT rh.hashTag as hashTag,
        COUNT(DISTINCT r.id) AS count
    FROM Auditorium a
    LEFT JOIN Seat s ON s.auditorium = a
    LEFT JOIN ReviewSeat rs ON rs.seat = s
    LEFT JOIN Review r ON rs.review = r
    LEFT JOIN ReviewHashTag rh ON rh.review = r
    WHERE a.id = :auditoriumId
    GROUP BY rh.hashTag.id, rh.hashTag.name
    ORDER BY COUNT(DISTINCT r.id) DESC, rh.hashTag.id DESC
    """)
    List<ReviewHashTagWithCount> findByAuditorium_Id(@Param("auditoriumId") String auditoriumId);


}
