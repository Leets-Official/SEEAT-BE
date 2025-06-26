package com.seeat.server.domain.manage.domain.repository;

import com.seeat.server.domain.manage.domain.entity.Feedback;
import org.springframework.data.jpa.repository.JpaRepository;

public interface FeedbackRepository extends JpaRepository<Feedback, Long> {
}
