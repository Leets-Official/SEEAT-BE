package com.seeat.server.domain.user.domain.repository;

import com.seeat.server.domain.user.domain.entity.User;
import com.seeat.server.domain.user.domain.entity.UserSocial;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findBySocialAndSocialId(UserSocial social, String socialId);

    Optional<User> findByEmail(String email);

    Optional<User> findByIdAndIsDeleteFalse(Long userId);

    List<User> findAllByIsDeleteTrueAndUpdatedAtBefore(LocalDateTime cutoffDate);

    // 테스트용 JPA
    @Modifying(clearAutomatically = true)
    @Query(
            "UPDATE User u " +
                    "SET u.updatedAt = :updatedAt " +
                    "WHERE u.id = :id")
    void updateUpdatedAtById(@Param("id") Long id, @Param("updatedAt") LocalDateTime updatedAt);
}
