package com.seeat.server.domain.theater.domain.repository;

import com.seeat.server.domain.theater.domain.entity.Auditorium;
import com.seeat.server.domain.theater.domain.entity.AuditoriumType;
import com.seeat.server.domain.theater.domain.repository.dto.AuditoriumWithScore;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface AuditoriumRepository extends JpaRepository<Auditorium, String> {

    /// 타입에 맞는 상영관 추출
    Slice<Auditorium> findByType(AuditoriumType type, Pageable pageable);

    @Query("""
    SELECT
        a AS auditorium,
        COUNT(r) AS reviewCount,
        AVG(r.rating) AS avgRating,
        (COUNT(r) * 0.3 + AVG(r.rating) * 0.7) AS score
    FROM Auditorium a
    LEFT JOIN Review r ON r.seat.auditorium = a
    GROUP BY a.id
    ORDER BY COUNT(r) * 0.3 + COALESCE(AVG(r.rating), 0) * 0.7 DESC
""")
    Slice<AuditoriumWithScore> findBestAuditoriums(Pageable pageable);

}
