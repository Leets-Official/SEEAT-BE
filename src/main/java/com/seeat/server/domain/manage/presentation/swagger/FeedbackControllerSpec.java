package com.seeat.server.domain.manage.presentation.swagger;

import com.seeat.server.domain.manage.application.dto.request.FeedbackRequest;
import com.seeat.server.domain.manage.application.dto.response.FeedbackDetailResponse;
import com.seeat.server.domain.user.domain.entity.User;
import com.seeat.server.global.response.ApiResponse;
import com.seeat.server.security.oauth2.application.dto.response.CustomUserInfo;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

@Tag(name = "피드백 API")
public interface FeedbackControllerSpec {

    /**
     * 피드백 작성 API
     */
    @Operation(
        summary = "피드백 작성",
        description = "피드백을 작성합니다."
    )
    @PostMapping
    ApiResponse<Void> createFeedback(
        @RequestBody @Valid FeedbackRequest request,
        @Parameter(hidden = true) @AuthenticationPrincipal CustomUserInfo user);

    /**
     * 피드백 상세 조회 API
     *
     * @param feedbackId 피드백 ID
     * @return 피드백 상세 정보
     */
    @Operation(
            summary = "피드백 상세 조회",
            description = "피드백 ID로 상세 정보를 조회합니다."
    )
    @GetMapping("/{feedbackId}")
    ApiResponse<FeedbackDetailResponse> getReview(
            @Parameter(example = "1")
            @PathVariable Long feedbackId,

            @Parameter(hidden = true) @AuthenticationPrincipal CustomUserInfo user
    );
}
