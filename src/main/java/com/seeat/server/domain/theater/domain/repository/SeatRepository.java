package com.seeat.server.domain.theater.domain.repository;

import com.seeat.server.domain.theater.domain.entity.Auditorium;
import com.seeat.server.domain.theater.domain.entity.Seat;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface SeatRepository extends JpaRepository<Seat, String> {

    List<Seat> findByAuditorium(Auditorium auditorium);

    List<Seat> findByIdIn(List<String> ids);

    Optional<Seat> findByRowAndColumnAndAuditorium_Id(String row, int column, String auditoriumId);
}
