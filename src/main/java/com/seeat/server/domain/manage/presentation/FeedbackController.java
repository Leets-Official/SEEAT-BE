package com.seeat.server.domain.manage.presentation;

import com.seeat.server.domain.manage.application.FeedbackUseCase;
import com.seeat.server.domain.manage.application.dto.FeedbackRequest;
import com.seeat.server.domain.manage.presentation.swagger.FeedbackControllerSpec;
import com.seeat.server.domain.review.application.dto.request.ReviewRequest;
import com.seeat.server.domain.review.application.usecase.ReviewUseCase;
import com.seeat.server.domain.user.domain.entity.User;
import com.seeat.server.global.response.ApiResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/feedback")
@RequiredArgsConstructor
public class FeedbackController implements FeedbackControllerSpec {

    private final FeedbackUseCase feedbackService;

    /**
     * 피드백 작성
     * @param request 피드백 작성을 위한 DTO
     * @param user Jwt 기반 SecurityContext 저장되어있는 유저
     * @return 피드백 작성 알림
     */
    @PostMapping(
            consumes = MediaType.MULTIPART_FORM_DATA_VALUE
    )
    public ApiResponse<Void> createFeedback(
            @ModelAttribute @Valid FeedbackRequest request,
            @AuthenticationPrincipal User user) {

        // 서비스 호출
        feedbackService.createFeedback(request, user.getId());

        return ApiResponse.created();
    }
}
