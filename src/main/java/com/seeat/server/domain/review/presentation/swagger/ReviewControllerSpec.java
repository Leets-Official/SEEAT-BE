package com.seeat.server.domain.review.presentation.swagger;

import com.seeat.server.domain.review.application.dto.request.ReviewRequest;
import com.seeat.server.domain.review.application.dto.response.ReviewDetailResponse;
import com.seeat.server.domain.review.application.dto.response.ReviewListResponse;
import com.seeat.server.domain.user.domain.entity.User;
import com.seeat.server.global.response.ApiResponse;
import com.seeat.server.global.response.pageable.PageRequest;
import com.seeat.server.global.response.pageable.SliceResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.MediaType;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@Tag(name = "리뷰 API")
public interface ReviewControllerSpec {

    /**
     * 리뷰 작성 API
     */
    @Operation(
            summary = "리뷰 작성",
            description = "리뷰를 작성합니다."
    )
    @PostMapping(
            consumes = MediaType.MULTIPART_FORM_DATA_VALUE
    )
    ApiResponse<Void> createReview(
            @ModelAttribute @Valid ReviewRequest request,
            @Parameter(hidden = true) @AuthenticationPrincipal User user
    );

    /**
     * 리뷰 상세 조회 API
     *
     * @param reviewId 리뷰 ID
     * @return 리뷰 상세 정보
     */
    @Operation(
            summary = "리뷰 상세 조회",
            description = "리뷰 ID로 상세 정보를 조회합니다."
    )
    @GetMapping("/{reviewId}")
    ApiResponse<ReviewDetailResponse> getReview(
            @Parameter(example = "1")
            @PathVariable Long reviewId
    );

    /**
     * 상영관별 리뷰 목록 조회 API
     *
     * @param auditoriumId 영화관 ID
     * @param pageRequest 페이지 요청 정보
     */
    @Operation(
            summary = "상영관별 리뷰 목록 조회",
            description = "상영관 ID로 리뷰 목록을 조회합니다."
    )
    @GetMapping("/auditorium/{auditoriumId}")
    ApiResponse<SliceResponse<ReviewListResponse>> getReviewsByAuditorium(
            @Parameter(example = "1")
            @PathVariable String auditoriumId,
            PageRequest pageRequest
    );

    /**
     * 좌석별 리뷰 목록 조회 API
     *
     * @param seatId 좌석 ID
     * @param pageRequest 페이지 요청 정보
     * @return 리뷰 목록 페이지
     */

    @Operation(
            summary = "좌석별 리뷰 목록 조회",
            description = "좌석 ID로 리뷰 목록을 조회합니다."
    )
    @GetMapping("/seat/{seatId}")
    ApiResponse<SliceResponse<ReviewListResponse>> getReviewsBySeat(
            @Parameter(example = "1")
            @PathVariable String seatId,
            PageRequest pageRequest
    );
}
