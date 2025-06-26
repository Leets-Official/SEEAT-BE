package com.seeat.server.domain.review.domain.repository;

import com.seeat.server.domain.review.domain.entity.Bookmark;
import org.springframework.data.jpa.repository.JpaRepository;

public interface BookmarkRepository extends JpaRepository<Bookmark, Long> {
}
