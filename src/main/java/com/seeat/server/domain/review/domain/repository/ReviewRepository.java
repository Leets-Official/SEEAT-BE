package com.seeat.server.domain.review.domain.repository;

import com.seeat.server.domain.review.domain.entity.Review;
import com.seeat.server.domain.review.domain.entity.custom.ReviewRepositoryCustom;
import com.seeat.server.domain.review.domain.repository.dto.ReviewWithLikeCount;
import com.seeat.server.domain.search.application.dto.request.ReviewSearchCondition;
import com.seeat.server.domain.user.domain.entity.User;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface ReviewRepository extends JpaRepository<Review, Long>, ReviewRepositoryCustom {


    /**
     * 좌석 기반 검색
     * @param seatId    좌석 ID
     * @param pageable  페이징
     */
    @Query("SELECT r AS review, COUNT(rl) AS likeCount " +
            "FROM Review r LEFT JOIN ReviewLike rl ON rl.review.id = r.id " +
            "WHERE r.seat.id = :seatId " +
            "GROUP BY r "+
            "ORDER BY COUNT(rl) DESC, r.createdAt DESC")
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
            "GROUP BY r "+
            "ORDER BY COUNT(rl) DESC, r.createdAt DESC")
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
            "GROUP BY r ")
    Optional<ReviewWithLikeCount> findReviewAndCountById(@Param("id") Long id);


    @Query("SELECT r AS review, COUNT(rl) AS likeCount " +
            "FROM Review r LEFT JOIN ReviewLike rl ON rl.review.id = r.id " +
            "WHERE r.id IN :reviewIds " +
            "GROUP BY r " +
            "ORDER BY COUNT(rl) DESC, r.createdAt DESC")
    List<ReviewWithLikeCount> findByReviewIds(@Param("reviewIds") List<Long> reviewIds);

    /**
     * 좋아요 수를 기준으로 인기 리뷰 목록을 조회합니다.
     * 최신순(createdAt)으로 정렬 기준이 보조적으로 적용됩니다.
     *
     * @param pageable 페이지네이션 정보
     * @return 좋아요 수가 많은 순으로 정렬된 리뷰 목록
     */
    @Query("SELECT r AS review, COUNT(rl) AS likeCount " +
            "FROM Review r LEFT JOIN ReviewLike rl ON rl.review.id = r.id " +
            "GROUP BY r "+
            "ORDER BY COUNT(rl) DESC, r.createdAt DESC")
    Slice<ReviewWithLikeCount> findBestReviews(Pageable pageable);


    /**
     * 나의 리뷰 검색
     *
     * @param pageable 페이징
     * @return Slice<ReviewWithLikeCount>
     */
    @Query(
            "SELECT r AS review, COUNT(rl) AS likeCount " +
                    "FROM Review r LEFT JOIN ReviewLike rl ON rl.review.id = r.id " +
                    "WHERE r.user.id = :userId " +
                    "GROUP BY r " +
                    "ORDER BY COUNT(rl) DESC, r.createdAt DESC ")
    Slice<ReviewWithLikeCount> findMyReviews(@Param("userId") Long userId, Pageable pageable);


    /**
     * 유저와 리뷰가 동일한지 체크
     * @param user  유저
     * @param id    아이디
     */
    Optional<Review> findByUserAndId(User user, Long id);

    /**
     * 검색 필터 조회
     *
     * @param condition 조건 DTO
     * @param pageable 페이징
     * @return Slice<Review> 응답
     */
    Slice<Review> searchReviewsWithFilters(ReviewSearchCondition condition, Pageable pageable);

}
