package com.seeat.server.domain.review.domain.repository;

import com.seeat.server.domain.review.domain.entity.HashTag;
import org.springframework.data.jpa.repository.JpaRepository;

public interface HashTagRepository extends JpaRepository<HashTag, Long> {
}
