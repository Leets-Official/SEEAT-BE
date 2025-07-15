package com.seeat.server.domain.review.domain.repository;

import com.seeat.server.domain.review.domain.entity.Review;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface ReviewRepository extends JpaRepository<Review, Long> {

    Page<Review> findBySeat_Id(String seatId, Pageable pageable);

    @Query("select r from Review r where r.seat.auditorium.theater.id = :theaterId")
    Page<Review> findByTheater_Id(@Param("theaterId") String theaterId, Pageable pageable);

    @Query(
            "SELECT r FROM Review r " +
                    "LEFT JOIN ReviewLike rl ON rl.review.id = r.id " +
                    "GROUP BY r.id " +
                    "ORDER BY COUNT(rl) DESC, r.createdAt DESC"
    )
    Slice<Review> findAllOrderByPopularity(Pageable pageable);

}
