package com.seeat.server.domain.manage.application;

import com.seeat.server.domain.manage.application.dto.request.FeedbackRequest;
import com.seeat.server.domain.manage.application.dto.response.FeedbackDetailResponse;
import com.seeat.server.domain.manage.domain.entity.Feedback;
import com.seeat.server.domain.manage.domain.repository.FeedbackRepository;
import com.seeat.server.domain.user.application.usecase.UserUseCase;
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
    private final UserUseCase userService;

    @Override
    public void createFeedback(FeedbackRequest request, Long userId) {
        // 유저 예외처리
        User user = userService.getUser(userId);

        // 피드백 내용 null 또는 공백 검증 추가
        if (request.getFeedbackContent() == null || request.getFeedbackContent().trim().isEmpty()) {
            throw new IllegalArgumentException(ErrorCode.INVALID_FEEDBACK_CONTENT.getMessage());
        }

        // 객체 생성
        Feedback requestFeedback = Feedback.of(user, request.getFeedbackContent());

        // DB 내 저장
        repository.save(requestFeedback);
    }

    /**
     * 피드백 ID 조회
     */
    @Override
    public FeedbackDetailResponse loadFeedback(Long feedbackId, Long userId) {

        // FeedbackId를 바탕으로 조회
        Feedback feedback = repository.findById(feedbackId)
                .filter(f -> f.getUser().getId().equals(userId))
                .orElseThrow(() -> new NoSuchElementException(ErrorCode.NOT_FEEDBACK.getMessage()));

        return FeedbackDetailResponse.from(feedback);
    }
}
