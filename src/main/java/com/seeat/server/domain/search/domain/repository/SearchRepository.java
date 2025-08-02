package com.seeat.server.domain.search.domain.repository;

import com.seeat.server.domain.search.domain.entity.Search;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;

import java.util.Optional;

public interface SearchRepository extends JpaRepository<Search, Long> {

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    Optional<Search> findByContent(String keyword);

}
