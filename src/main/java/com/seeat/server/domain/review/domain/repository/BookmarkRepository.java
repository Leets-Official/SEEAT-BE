package com.seeat.server.domain.review.domain.repository;

import com.seeat.server.domain.review.domain.entity.Bookmark;
import com.seeat.server.domain.review.domain.entity.Review;
import com.seeat.server.domain.user.domain.entity.User;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.data.jpa.repository.JpaRepository;

public interface BookmarkRepository extends JpaRepository<Bookmark, Long> {


    boolean existsByIdAndUser(Long id, User user);

    boolean existsByUserAndReview(User user, Review review);

    Slice<Bookmark> findByUser(User user, Pageable pageable);

}
