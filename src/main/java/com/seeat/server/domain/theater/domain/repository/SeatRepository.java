package com.seeat.server.domain.theater.domain.repository;

import com.seeat.server.domain.theater.domain.entity.Auditorium;
import com.seeat.server.domain.theater.domain.entity.Seat;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface SeatRepository extends JpaRepository<Seat, String> {

    List<Seat> findByAuditorium(Auditorium auditorium);
}
