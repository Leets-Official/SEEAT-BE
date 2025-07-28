package com.seeat.server.domain.manage.application;

import com.seeat.server.domain.manage.application.dto.request.FeedbackRequest;
import com.seeat.server.domain.manage.application.dto.response.FeedbackDetailResponse;
import com.seeat.server.domain.manage.domain.entity.Feedback;
import com.seeat.server.domain.manage.domain.repository.FeedbackRepository;
import com.seeat.server.domain.user.application.UserUseCase;
import com.seeat.server.domain.user.domain.entity.User;
import com.seeat.server.global.response.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.NoSuchElementException;

@Service
@Transactional
@RequiredArgsConstructor
public class FeedbackService implements FeedbackUseCase{

    private final FeedbackRepository repository;
    private final UserUseCase userRepository;

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

    /**
     * 피드백 ID 조회
     */
    @Override
    public FeedbackDetailResponse loadFeedback(Long feedbackId) {

        // FeedbackId를 바탕으로 조회
        Feedback feedback = repository.findById(feedbackId)
            .orElseThrow(() -> new NoSuchElementException(ErrorCode.NOT_FEEDBACK.getMessage()));

        return FeedbackDetailResponse.from(feedback);
    }
}
