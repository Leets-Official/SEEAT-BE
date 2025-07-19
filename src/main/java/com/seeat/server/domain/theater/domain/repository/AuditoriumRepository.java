package com.seeat.server.domain.theater.domain.repository;

import com.seeat.server.domain.theater.domain.entity.Auditorium;
import com.seeat.server.domain.theater.domain.entity.AuditoriumType;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AuditoriumRepository extends JpaRepository<Auditorium, String> {

    /// 타입에 맞는 상영관 추출
    Slice<Auditorium> findByType(AuditoriumType type, Pageable pageable);

}
