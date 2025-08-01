package com.seeat.server.domain.image.domain.repository;

import com.seeat.server.domain.review.domain.entity.Review;
import com.seeat.server.domain.image.domain.entity.ReviewImage;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ReviewImageRepository extends JpaRepository<ReviewImage, Long> {
    List<ReviewImage> findByReview(Review review);

    void deleteAllByImageUrlIn(List<String> imageUrls);
}
