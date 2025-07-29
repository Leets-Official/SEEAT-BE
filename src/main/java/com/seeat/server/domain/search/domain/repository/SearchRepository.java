package com.seeat.server.domain.search.domain.repository;

import com.seeat.server.domain.search.domain.entity.Search;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface SearchRepository extends JpaRepository<Search, Long> {

    Optional<Search> findByContent(String keyword);

}
