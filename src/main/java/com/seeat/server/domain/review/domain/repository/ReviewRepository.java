package com.seeat.server.domain.review.domain.repository;

import com.seeat.server.domain.review.domain.entity.Review;
import com.seeat.server.domain.review.domain.repository.custom.ReviewRepositoryCustom;
import com.seeat.server.domain.review.domain.repository.dto.ReviewLikeCount;
import com.seeat.server.domain.review.domain.repository.dto.ReviewWithLikeCount;
import com.seeat.server.domain.review.domain.repository.dto.ReviewSeatStats;
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
     * 좌석의 리뷰 정보 조회
     *
     * @param seatId 조회할 좌석 ID
     */
    @Query("""
                SELECT s AS seat,
                       COUNT(r) AS reviewCount,
                       AVG(r.rating) AS averageRating
                FROM Seat s
                LEFT JOIN Review r ON r.seat = s
                WHERE s.id = :seatId
                GROUP BY s
            """)
    ReviewSeatStats findSeatReviewStats(@Param("seatId") String seatId);

    // ========================
    //  좌석별 함수
    // ========================

    /**
     * 좌석별 최신순(기본, 생성일자 내림차순) 리뷰 조회
     */
    @Query("SELECT r AS review, COUNT(rl) AS likeCount "
            + "FROM Review r LEFT JOIN ReviewLike rl ON rl.review.id = r.id "
            + "WHERE r.seat.id = :seatId "
            + "GROUP BY r "
            + "ORDER BY r.createdAt DESC")
    Slice<ReviewWithLikeCount> findBySeat_IdOrderByLatest(@Param("seatId") String seatId, Pageable pageable);

    /**
     * 좌석별 좋아요 많은 순(내림차순) 리뷰 조회
     * @param seatId    좌석 ID
     * @param pageable  페이징 정보
     * @return 리뷰와 좋아요 수
     */
    @Query("SELECT r AS review, COUNT(rl) AS likeCount " +
            "FROM Review r LEFT JOIN ReviewLike rl ON rl.review.id = r.id " +
            "WHERE r.seat.id = :seatId " +
            "GROUP BY r " +
            "ORDER BY COUNT(rl) DESC, r.createdAt DESC")
    Slice<ReviewWithLikeCount> findBySeat_IdOrderByLikesDesc(@Param("seatId") String seatId, Pageable pageable);

    /**
     * 좌석별 평점 높은 순(내림차순) 리뷰 조회
     * @param seatId    좌석 ID
     * @param pageable  페이징 정보
     * @return 리뷰와 좋아요 수
     */
    @Query("SELECT r AS review, COUNT(rl) AS likeCount " +
            "FROM Review r LEFT JOIN ReviewLike rl ON rl.review.id = r.id " +
            "WHERE r.seat.id = :seatId " +
            "GROUP BY r " +
            "ORDER BY r.rating DESC, r.createdAt DESC")
    Slice<ReviewWithLikeCount> findBySeat_IdOrderByRatingDesc(@Param("seatId") String seatId, Pageable pageable);

    /**
     * 좌석별 평점 낮은 순(오름차순) 리뷰 조회
     * @param seatId    좌석 ID
     * @param pageable  페이징 정보
     * @return 리뷰와 좋아요 수
     */
    @Query("SELECT r AS review, COUNT(rl) AS likeCount " +
            "FROM Review r LEFT JOIN ReviewLike rl ON rl.review.id = r.id " +
            "WHERE r.seat.id = :seatId " +
            "GROUP BY r " +
            "ORDER BY r.rating ASC, r.createdAt DESC")
    Slice<ReviewWithLikeCount> findBySeat_IdOrderByRatingAsc(@Param("seatId") String seatId, Pageable pageable);

    // ========================
    //  상영관별 함수
    // ========================

    /**
     * 상영관별 최신순(기본, 생성일자 내림차순) 리뷰 조회
     */
    @Query("SELECT r AS review, COUNT(rl) AS likeCount "
            + "FROM Review r LEFT JOIN ReviewLike rl ON rl.review.id = r.id "
            + "WHERE r.seat.auditorium.id = :auditoriumId "
            + "GROUP BY r "
            + "ORDER BY r.createdAt DESC")
    Slice<ReviewWithLikeCount> findByAuditorium_IdOrderByLatest(@Param("auditoriumId") String auditoriumId, Pageable pageable);

    /**
     * 상영관별 좋아요 많은 순(내림차순) 리뷰 조회
     * @param auditoriumId 상영관 ID
     * @param pageable     페이징 정보
     * @return 리뷰와 좋아요 수
     */
    @Query("SELECT r AS review, COUNT(rl) AS likeCount " +
            "FROM Review r LEFT JOIN ReviewLike rl ON rl.review.id = r.id " +
            "WHERE r.seat.auditorium.id = :auditoriumId " +
            "GROUP BY r " +
            "ORDER BY COUNT(rl) DESC, r.createdAt DESC")
    Slice<ReviewWithLikeCount> findByAuditorium_IdOrderByLikesDesc(@Param("auditoriumId") String auditoriumId, Pageable pageable);

    /**
     * 상영관별 평점 높은 순(내림차순) 리뷰 조회
     * @param auditoriumId 상영관 ID
     * @param pageable     페이징 정보
     * @return 리뷰와 좋아요 수
     */
    @Query("SELECT r AS review, COUNT(rl) AS likeCount " +
            "FROM Review r LEFT JOIN ReviewLike rl ON rl.review.id = r.id " +
            "WHERE r.seat.auditorium.id = :auditoriumId " +
            "GROUP BY r " +
            "ORDER BY r.rating DESC, r.createdAt DESC")
    Slice<ReviewWithLikeCount> findByAuditorium_IdOrderByRatingDesc(@Param("auditoriumId") String auditoriumId, Pageable pageable);

    /**
     * 상영관별 평점 낮은 순(오름차순) 리뷰 조회
     * @param auditoriumId 상영관 ID
     * @param pageable     페이징 정보
     * @return 리뷰와 좋아요 수
     */
    @Query("SELECT r AS review, COUNT(rl) AS likeCount " +
            "FROM Review r LEFT JOIN ReviewLike rl ON rl.review.id = r.id " +
            "WHERE r.seat.auditorium.id = :auditoriumId " +
            "GROUP BY r " +
            "ORDER BY r.rating ASC, r.createdAt DESC")
    Slice<ReviewWithLikeCount> findByAuditorium_IdOrderByRatingAsc(@Param("auditoriumId") String auditoriumId, Pageable pageable);

    /**
     * 상영관 기반으로 리뷰가 존재하는지 여부 체크
     * @param auditoriumId  상영관
     * @return   boolean
     */
    @Query("SELECT COUNT(r) " +
            "FROM Review r "+
            "WHERE r.seat.auditorium.id = :auditoriumId")
    Long countByAuditoriumId(@Param("auditoriumId") String auditoriumId);


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

    /**
     * 리뷰 개수와 하트 개수 조회
     *
     * @param userId 유저 Id
     * @return ReviewLikeCountResponse 응답
     */
    @Query(" SELECT COUNT(DISTINCT r.id) as reviewCount,COUNT(rl.id) as likeCount " +
            "FROM Review r " +
            "LEFT JOIN ReviewLike rl " +
            "ON rl.review.id = r.id " +
            "AND rl.user.id != :userId " +
            "WHERE r.user.id = :userId")
    ReviewLikeCount findReviewCountAndLikeCountByUserId(@Param("userId") Long userId);
           
    /**
     * 같이 작성된 리뷰 조회
     * @param groupId   같이 작성된 그룹 ID
     */
    List<Review> findByGroupId(String groupId);
}
