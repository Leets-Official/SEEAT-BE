package com.seeat.server.domain.theater.domain.repository;


import com.seeat.server.domain.theater.domain.entity.Seat;
import com.seeat.server.domain.theater.domain.entity.SeatRatingSummary;
import com.seeat.server.domain.theater.domain.repository.dto.SeatWithRating;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.*;

public interface SeatRatingSummaryRepository extends JpaRepository<SeatRatingSummary,Long> {

    Optional<SeatRatingSummary> findBySeat(Seat seat);

    @Query("""
    select s as seat,
           sr.totalReviews as totalReviews,
           sr.averageGrade as averageRating
    from Seat s
    left join SeatRatingSummary sr on sr.seat = s
    where s.auditorium.id = :auditoriumId
""")
    List<SeatWithRating> findRatingsByAuditoriumId(@Param("auditoriumId") String auditoriumId);



}
