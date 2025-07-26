package com.seeat.server.domain.manage.application;

import com.seeat.server.domain.manage.application.dto.FeedbackRequest;
import com.seeat.server.domain.review.application.dto.request.ReviewRequest;

public interface FeedbackUseCase {

    /// 피드백 작성
    void createFeedback(FeedbackRequest request, Long userId);
}
