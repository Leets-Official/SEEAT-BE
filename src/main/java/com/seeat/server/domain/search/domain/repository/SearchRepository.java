package com.seeat.server.domain.search.domain.repository;

import com.seeat.server.domain.search.domain.entity.Search;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SearchRepository extends JpaRepository<Search, Long> {

}
