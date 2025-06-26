package com.seeat.server.domain.user.domain.repository;

import com.seeat.server.domain.user.domain.entity.UserTheater;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserTheaterRepository extends JpaRepository<UserTheater, Long> {
}
