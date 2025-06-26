package com.seeat.server.domain.review.domain.repository;

import com.seeat.server.domain.review.domain.entity.ReviewImage;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ReviewImageRepository extends JpaRepository<ReviewImage, Long> {
}
