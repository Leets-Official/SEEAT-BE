package com.seeat.server.domain.user.domain.repository;

import com.seeat.server.domain.search.domain.entity.Search;
import com.seeat.server.domain.user.domain.entity.UserSearch;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface UserSearchRepository extends JpaRepository<UserSearch, Long> {
    @Query("""
    SELECT us.search
    FROM UserSearch us
    WHERE us.user.id = :userId
    ORDER BY us.createdAt DESC
    """)
    List<Search> findSearchListByUserId(@Param("userId") Long userId);

}
