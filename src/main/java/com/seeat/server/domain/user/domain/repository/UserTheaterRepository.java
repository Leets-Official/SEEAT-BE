package com.seeat.server.domain.user.domain.repository;

import com.seeat.server.domain.user.domain.entity.User;
import com.seeat.server.domain.user.domain.entity.UserTheater;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface UserTheaterRepository extends JpaRepository<UserTheater, Long> {

    List<UserTheater> findByUserId(long id);
}
