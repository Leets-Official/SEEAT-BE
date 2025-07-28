package com.seeat.server.domain.user.domain.repository;

import com.seeat.server.domain.theater.domain.entity.Auditorium;
import com.seeat.server.domain.user.domain.entity.UserAuditorium;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface UserAuditoriumRepository extends JpaRepository<UserAuditorium, Long> {

    @Query("""
    SELECT DISTINCT ua.auditorium
    FROM UserAuditorium ua
    WHERE ua.user.id = :userId
    """)
    List<Auditorium> findDistinctAuditoriumsByUserId(@Param("userId") Long userId);

    void deleteByUserId(Long userId);
}
