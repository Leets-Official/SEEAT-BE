package com.seeat.server.domain.review.domain.repository;

import com.seeat.server.domain.review.domain.entity.ReviewLike;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ReviewLikeRepository extends JpaRepository<ReviewLike, Long> {
}
