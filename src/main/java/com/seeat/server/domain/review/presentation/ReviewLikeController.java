package com.seeat.server.domain.review.presentation;

import com.seeat.server.domain.review.application.usecase.ReviewLikeUseCase;
import com.seeat.server.domain.review.presentation.swagger.ReviewLikeControllerSpec;
import com.seeat.server.domain.user.domain.entity.User;
import com.seeat.server.global.response.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/reviews/likes")
@RequiredArgsConstructor
public class ReviewLikeController implements ReviewLikeControllerSpec {

    private final ReviewLikeUseCase service;

    /**
     * 좋아요를 생성하는 API 입니다.
     * @param user          로그인한 유저
     * @param reviewId      리뷰 ID
     */
    @PostMapping
    public ApiResponse<Void> reviewLike(
            @AuthenticationPrincipal User user,
            @RequestParam Long reviewId) {

        /// 서비스 호출
        service.reviewLike(user.getId(), reviewId);


        /// 리턴
        return ApiResponse.created();
    }


    /**
     * 좋아요를 삭제하는 API 입니다.
     * @param user          로그인한 유저
     * @param reviewId      리뷰 ID
     */
    @DeleteMapping
    public ApiResponse<Void> deleteReviewLike(
            @AuthenticationPrincipal User user,
            @RequestParam Long reviewId){

        /// 서비스 호출
        service.reviewCancel(user.getId(), reviewId);

        /// 리턴
        return ApiResponse.deleted();
    }

}
