package com.seeat.server.domain.review.presentation;

import com.seeat.server.domain.review.application.dto.request.ReviewUpdateRequest;
import com.seeat.server.domain.review.application.usecase.ReviewUseCase;
import com.seeat.server.domain.review.application.dto.request.ReviewRequest;
import com.seeat.server.domain.review.application.dto.response.ReviewDetailResponse;
import com.seeat.server.domain.review.application.dto.response.ReviewListResponse;
import com.seeat.server.domain.review.presentation.swagger.ReviewControllerSpec;
import com.seeat.server.domain.user.domain.entity.User;
import com.seeat.server.global.response.ApiResponse;
import com.seeat.server.global.response.pageable.PageRequest;
import com.seeat.server.global.response.pageable.SliceResponse;
import io.swagger.v3.oas.annotations.Parameter;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;

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
    @PostMapping()
    public ApiResponse<Void> createReview(
            @RequestBody @Valid ReviewRequest request,
            @AuthenticationPrincipal User user) throws IOException {

        // 서비스 호출
        reviewService.createReview(request, user.getId());

        // 결과 리턴
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
     * 상영관에 따른 리뷰 목록 조회
     * @param auditoriumId 상영관에 따른 리뷰 목록 조회를 위한 Id
     * @return Page<ReviewListResponse> Page DTO
     */
    @GetMapping("/auditorium/{auditoriumId}")
    public ApiResponse<SliceResponse<ReviewListResponse>> getReviewsByAuditorium(
            @PathVariable String auditoriumId,
            PageRequest pageRequest) {

        // 서비스 호출
        SliceResponse<ReviewListResponse> response = reviewService.loadReviewsByAuditoriumId(auditoriumId, pageRequest);

        // 결과 리턴
        return ApiResponse.ok(response);
    }


    /**
     * 자리에 따른 리뷰 목록 조회
     * @param seatId 영화관 자리에 따른 리뷰 목록 조회를 위한 Id
     * @return Page<ReviewListResponse> Page DTO
     */
    @GetMapping("/seat/{seatId}")
    public ApiResponse<SliceResponse<ReviewListResponse>> getReviewsBySeat(
            @PathVariable String seatId,
            PageRequest pageRequest) {

        // 서비스 호출
        SliceResponse<ReviewListResponse> response = reviewService.loadReviewsBySeatId(seatId, pageRequest);

        // 결과 리턴
        return ApiResponse.ok(response);
    }

    /**
     * 리뷰 수정
     *
     * @param request 수정 DTO
     * @param user    유저
     */
    @PatchMapping(path = "/{reviewId}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ApiResponse<Void> updateReview(
            @PathVariable Long reviewId,
            @RequestBody @Valid ReviewUpdateRequest request,
            @AuthenticationPrincipal User user) throws IOException {

        /// 서비스 호출
        reviewService.updateReview(reviewId, request, user.getId());

        return ApiResponse.updated();
    }

    /**
     * 리뷰 삭제
     * @param reviewId  리뷰ID
     * @param user      유저
     */
    @DeleteMapping("/{reviewId}")
    public ApiResponse<Void> deleteReview(
            @PathVariable Long reviewId,
            @Parameter(hidden = true) @AuthenticationPrincipal User user) throws IOException {

        /// 서비스 호출
        reviewService.deleteReview(reviewId, user.getId());

        // 결과 리턴
        return ApiResponse.deleted();
    }

}
