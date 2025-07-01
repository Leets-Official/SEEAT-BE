package com.seeat.server.domain.review.domain.repository;

import com.seeat.server.domain.review.domain.entity.Review;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.*;

public interface ReviewRepository extends JpaRepository<Review, Long> {

    List<Review> findBySeat_Id(Long seatId);

}
