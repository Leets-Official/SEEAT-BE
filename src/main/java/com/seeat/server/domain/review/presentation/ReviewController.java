package com.seeat.server.domain.review.presentation;

import com.seeat.server.domain.review.application.usecase.ReviewUseCase;
import com.seeat.server.domain.review.presentation.dto.request.ReviewRequest;
import com.seeat.server.domain.review.presentation.dto.response.ReviewDetailResponse;
import com.seeat.server.domain.review.presentation.swagger.ReviewControllerSpec;
import com.seeat.server.domain.user.domain.entity.User;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/reviews")
public class ReviewController implements ReviewControllerSpec {

    private final ReviewUseCase reviewService;

    /**
     * 리뷰 작성
     * @param request 리뷰 작성을 위한 DTO
     * @param user Jwt 기반 SecurityContext 저장되어있는 유저
     * @return 리뷰 작성 알림
     */
    @PostMapping
    public String createReview(
            @RequestBody ReviewRequest request,
            @AuthenticationPrincipal User user) {

        // 서비스 호출
        reviewService.createReview(request, user.getId());

        return "리뷰가 생성되었습니다.";
    }

    /**
     * 리뷰 상세 조회
     * @param reviewId 리뷰 상세 조회를 위한 Id
     * @return ReviewDetailResponse DTO 작성 응답
     */
    @GetMapping("/{reviewId}")
    public ReviewDetailResponse getReview(@PathVariable Long reviewId) {

        // 서비스 호출
        return reviewService.loadReview(reviewId);
    }




}
