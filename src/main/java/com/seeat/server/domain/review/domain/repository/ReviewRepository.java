package com.seeat.server.domain.review.domain.repository;

import com.seeat.server.domain.review.domain.entity.Review;
import com.seeat.server.domain.review.domain.repository.dto.ReviewWithLikeCount;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.util.*;

public interface ReviewRepository extends JpaRepository<Review, Long> {


    /**
     * 좌석 기반 검색
     * @param seatId    좌석 ID
     * @param pageable  페이징
     */
    @Query("SELECT r AS review, COUNT(rl) AS likeCount " +
            "FROM Review r LEFT JOIN ReviewLike rl ON rl.review.id = r.id " +
            "WHERE r.seat.id = :seatId " +
            "GROUP BY r")
    Slice<ReviewWithLikeCount> findBySeat_Id(@Param("seatId") String seatId, Pageable pageable);


    /**
     * 상영관 기반 검색
     * @param auditoriumId  상영관
     * @param pageable      페이징
     * @return   Page<ReviewWithLikeCount>
     */
    @Query("SELECT r AS review, COUNT(rl) AS likeCount " +
            "FROM Review r LEFT JOIN ReviewLike rl ON rl.review.id = r.id " +
            "WHERE r.seat.auditorium.id = :auditoriumId " +
            "GROUP BY r")
    Slice<ReviewWithLikeCount> findByAuditorium_Id(@Param("auditoriumId") String auditoriumId, Pageable pageable);

    /**
     * 인기순 검색
     * @param pageable  페이징
     * @return Slice<ReviewWithLikeCount>
     */
    @Query(
            "SELECT r AS review, COUNT(rl) AS likeCount " +
                    "FROM Review r LEFT JOIN ReviewLike rl ON rl.review.id = r.id " +
                    "GROUP BY r " +
                    "ORDER BY COUNT(rl) DESC, r.createdAt DESC"
    )
    Slice<ReviewWithLikeCount> findAllOrderByPopularity(Pageable pageable);

    /**
     * Id 바탕으로 좋아요, 리뷰 상세 검색
     * @param id    리뷰 ID
     * @return Optional<ReviewWithLikeCount>
     */
    @Query("SELECT r AS review, COUNT(rl) AS likeCount " +
            "FROM Review r LEFT JOIN ReviewLike rl ON rl.review.id = r.id " +
            "WHERE r.id = :id " +
            "GROUP BY r")
    Optional<ReviewWithLikeCount> findReviewAndCountById(@Param("id") Long id);


    @Query("SELECT r AS review, COUNT(rl) AS likeCount " +
            "FROM Review r LEFT JOIN ReviewLike rl ON rl.review.id = r.id " +
            "WHERE r.id IN :reviewIds " +
            "GROUP BY r")
    List<ReviewWithLikeCount> findByReviewIds(@Param("reviewIds") List<Long> reviewIds);


}
