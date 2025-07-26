package com.seeat.server.domain.manage.application;

import com.seeat.server.domain.manage.application.dto.FeedbackRequest;
import com.seeat.server.domain.manage.domain.entity.Feedback;
import com.seeat.server.domain.manage.domain.repository.FeedbackRepository;
import com.seeat.server.domain.review.domain.entity.Review;
import com.seeat.server.domain.theater.domain.entity.Seat;
import com.seeat.server.domain.user.domain.entity.User;
import com.seeat.server.domain.user.domain.repository.UserRepository;
import com.seeat.server.global.response.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.data.jpa.repository.support.SimpleJpaRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.NoSuchElementException;

@Service
@Transactional
@RequiredArgsConstructor
public class FeedbackService implements FeedbackUseCase{

    private final FeedbackRepository repository;
    private final UserRepository userRepository;

    @Override
    public void createFeedback(FeedbackRequest request, Long userId) {
        // 유저 예외처리
        User user = userRepository.findById(userId)
            .orElseThrow(() -> new NoSuchElementException(ErrorCode.NOT_USER.getMessage()));

        // 객체 생성
        Feedback requestFeedback = Feedback.of(user, request.getFeedbackContent());

        // DB 내 저장
        repository.save(requestFeedback);
    }
}
