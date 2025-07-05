package com.seeat.server.domain.user.domain.repository;

import com.seeat.server.domain.user.domain.entity.User;
import com.seeat.server.domain.user.domain.entity.UserSocial;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findBySocialAndSocialId(UserSocial social, String socialId);

    Optional<User> findByEmail(String email);
}
