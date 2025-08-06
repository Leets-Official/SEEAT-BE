package com.seeat.server.domain.theater.domain.repository;

import com.seeat.server.domain.theater.domain.entity.Auditorium;
import com.seeat.server.domain.theater.domain.entity.AuditoriumType;
import com.seeat.server.domain.theater.domain.repository.dto.AuditoriumWithScore;
import com.seeat.server.domain.theater.domain.repository.dto.AuditoriumWithRating;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface AuditoriumRepository extends JpaRepository<Auditorium, String> {

    /// 타입에 맞는 상영관 추출
    Slice<Auditorium> findByType(AuditoriumType type, Pageable pageable);


    /// 리뷰 개수 및 평점 같이 가져오기
    @Query("""
    SELECT
        a AS auditorium,
        COUNT(DISTINCT r.id) AS totalReviews,
        AVG(DISTINCT r.rating) AS averageRating
    FROM Auditorium a
    LEFT JOIN Seat s ON s.auditorium = a
    LEFT JOIN ReviewSeat rs ON rs.seat = s
    LEFT JOIN Review r ON rs.review = r
    WHERE a.id = :auditoriumId and r.user.isDelete = false
    GROUP BY a
    """)
    Optional<AuditoriumWithRating> findAuditoriumWithRating(@Param("auditoriumId") String auditoriumId);


    @Query("""
    SELECT
        a AS auditorium,
        COUNT(DISTINCT r.id) AS reviewCount,
        AVG(r.rating) AS avgRating,
        (COUNT(DISTINCT r.id) * 0.3 + AVG(r.rating) * 0.7) AS score
    FROM Auditorium a
    LEFT JOIN Seat s ON s.auditorium = a
    LEFT JOIN ReviewSeat rs ON rs.seat = s
    LEFT JOIN Review r ON rs.review = r
    where r.user.isDelete = false
    GROUP BY a.id
    ORDER BY (COUNT(DISTINCT r.id) * 0.3 + COALESCE(AVG(r.rating), 0) * 0.7) DESC
""")
    Slice<AuditoriumWithScore> findBestAuditoriums(Pageable pageable);



    Optional<Auditorium> findByNameContainingIgnoreCaseAndTheater_Id(String name, String theater_id);
}
