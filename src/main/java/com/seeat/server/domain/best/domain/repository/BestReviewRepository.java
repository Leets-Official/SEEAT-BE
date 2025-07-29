package com.seeat.server.domain.best.domain.repository;

import com.seeat.server.domain.best.domain.entity.BestReviewSnapshot;
import org.springframework.data.jpa.repository.JpaRepository;

public interface BestReviewRepository extends JpaRepository<BestReviewSnapshot, Long> {
    boolean existsByReviewId(Long reviewId);
}
