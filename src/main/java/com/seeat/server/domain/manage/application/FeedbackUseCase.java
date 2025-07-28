package com.seeat.server.domain.manage.application;

import com.seeat.server.domain.manage.application.dto.request.FeedbackRequest;
import com.seeat.server.domain.manage.application.dto.response.FeedbackDetailResponse;

public interface FeedbackUseCase {

    /// 피드백 작성
    void createFeedback(FeedbackRequest request, Long userId);

    FeedbackDetailResponse loadFeedback(Long feedbackId);
}
