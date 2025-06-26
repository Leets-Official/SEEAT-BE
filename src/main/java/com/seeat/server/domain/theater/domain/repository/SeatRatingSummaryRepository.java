package com.seeat.server.domain.theater.domain.repository;

import com.seeat.server.domain.theater.domain.entity.SeatRatingSummary;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SeatRatingSummaryRepository extends JpaRepository<SeatRatingSummary,Long> {
}
