package com.seeat.server.domain.review.presentation.swagger;

import com.seeat.server.domain.user.domain.entity.User;
import com.seeat.server.global.response.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.RequestParam;

@Tag(name = "좋아요 API", description = "리뷰 좋아요 API ")
public interface ReviewLikeControllerSpec {

    @Operation(
            summary = "좋아요 API",
            description = "JWT를 기반으로 리뷰에 좋아요를 추가합니다."
    )
    ApiResponse<Void> reviewLike(
            @AuthenticationPrincipal User user,
            @RequestParam Long reviewId);

    @Operation(
            summary = "좋아요 삭제 API",
            description = "JWT를 기반으로 좋아요를 삭제합니다."
    )
    ApiResponse<Void> deleteReviewLike(
            @AuthenticationPrincipal User user,
            @RequestParam Long reviewId);



}
