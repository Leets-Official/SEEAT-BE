package com.seeat.server.domain.review.presentation;

import com.seeat.server.domain.review.application.usecase.ReviewUseCase;
import com.seeat.server.domain.review.application.dto.request.ReviewRequest;
import com.seeat.server.domain.review.application.dto.response.ReviewDetailResponse;
import com.seeat.server.domain.review.application.dto.response.ReviewListResponse;
import com.seeat.server.domain.review.presentation.swagger.ReviewControllerSpec;
import com.seeat.server.domain.user.domain.entity.User;
import com.seeat.server.global.response.ApiResponse;
import com.seeat.server.global.response.pageable.PageRequest;
import com.seeat.server.global.response.pageable.PageResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
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
    @PostMapping(
            consumes = MediaType.MULTIPART_FORM_DATA_VALUE
    )
    public ApiResponse<Void> createReview(
            @ModelAttribute @Valid ReviewRequest request,
            @AuthenticationPrincipal User user) {

        // 서비스 호출
        reviewService.createReview(request, user.getId());

        return ApiResponse.created();
    }

    /**
     * 리뷰 상세 조회
     * @param reviewId 리뷰 상세 조회를 위한 Id
     * @return ReviewDetailResponse DTO 작성 응답
     */
    @GetMapping("/{reviewId}")
    public ApiResponse<ReviewDetailResponse> getReview(
            @PathVariable Long reviewId) {

        // 서비스 호출
        var response = reviewService.loadReview(reviewId);

        // 결과 리턴
        return ApiResponse.ok(response);
    }

    /**
     * 영화관에 따른 리뷰 목록 조회
     * @param theaterId 영화관에 따른 리뷰 목록 조회를 위한 Id
     * @return Page<ReviewListResponse> Page DTO
     */
    @GetMapping("/theater/{theaterId}")
    public ApiResponse<PageResponse<ReviewListResponse>> getReviewsByTheater(
            @PathVariable String theaterId,
            PageRequest pageRequest) {

        // 서비스 호출
        PageResponse<ReviewListResponse> response = reviewService.loadReviewsBySeatId(theaterId, pageRequest);

        // 응답
        return ApiResponse.ok(response);
    }


    /**
     * 자리에 따른 리뷰 목록 조회
     * @param seatId 영화관 자리에 따른 리뷰 목록 조회를 위한 Id
     * @return Page<ReviewListResponse> Page DTO
     */
    @GetMapping("/seat/{seatId}")
    public ApiResponse<PageResponse<ReviewListResponse>> getReviewsBySeat(
            @PathVariable String seatId,
            PageRequest pageRequest) {

        // 서비스 호출
        PageResponse<ReviewListResponse> response = reviewService.loadReviewsBySeatId(seatId, pageRequest);

        return ApiResponse.ok(response);
    }




}
