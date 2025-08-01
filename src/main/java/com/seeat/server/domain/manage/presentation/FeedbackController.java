package com.seeat.server.domain.manage.presentation;

import com.seeat.server.domain.manage.application.FeedbackUseCase;
import com.seeat.server.domain.manage.application.dto.request.FeedbackRequest;
import com.seeat.server.domain.manage.application.dto.response.FeedbackDetailResponse;
import com.seeat.server.domain.manage.presentation.swagger.FeedbackControllerSpec;
import com.seeat.server.domain.user.domain.entity.User;
import com.seeat.server.global.response.ApiResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

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
    @PostMapping
    public ApiResponse<Void> createFeedback(
            @RequestBody @Valid FeedbackRequest request,
            @AuthenticationPrincipal User user) {

        // 서비스 호출
        feedbackService.createFeedback(request, user.getId());

        return ApiResponse.created();
    }

    /**
     * 피드백 상세 조회
     * @param feedbackId 피드백 상세 조회를 위한 Id
     * @return FeedbackDetailResponse DTO 작성 응답
     */
    @GetMapping("/{feedbackId}")
    public ApiResponse<FeedbackDetailResponse> getReview(
            @PathVariable Long feedbackId,
            @AuthenticationPrincipal User user) {

        // 서비스 호출
        var response = feedbackService.loadFeedback(feedbackId, user.getId());

        // 결과 리턴
        return ApiResponse.ok(response);
    }
}
