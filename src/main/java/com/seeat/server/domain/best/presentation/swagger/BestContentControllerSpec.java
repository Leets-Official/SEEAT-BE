package com.seeat.server.domain.best.presentation.swagger;

import com.seeat.server.domain.best.application.dto.response.BestReviewListResponse;
import com.seeat.server.domain.review.application.dto.response.ReviewListResponse;
import com.seeat.server.domain.best.application.dto.response.BestAuditoriumListResponse;
import com.seeat.server.global.response.ApiResponse;
import com.seeat.server.global.response.pageable.PageRequest;
import com.seeat.server.global.response.pageable.SliceResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;

@Tag(name = "베스트 목록 API", description = "홈 화면에서 사용할 베스트 목록 API 입니다.")
public interface BestContentControllerSpec {

    /**
     * 인기 후기 조회 API
     * @param pageRequest   페이지
     */
    @Operation(
            summary = "인기 후기 목록 조회 API",
            description = "인기있는 후기를 조회할 API 입니다"
    )
    ApiResponse<SliceResponse<BestReviewListResponse>> getBestReviews(
            PageRequest pageRequest);


    /**
     * 인기 상영관 조회 API
     * @param pageRequest   페이지
     */
    @Operation(
            summary = "인기 상영관 목록 조회 API",
            description = "인기있는 상영관을 조회할 API 입니다"
    )
    ApiResponse<SliceResponse<BestAuditoriumListResponse>> getBestAuditoriums(
            PageRequest pageRequest);

    /**
     * 인기 후기/상영관 최신화 API
     */
    @Operation(
            summary = "인기 후기/상영관 수동 최신화 API",
            description = "인기있는 후기/상영관을 수동으로 최신화할 수 있는 API 입니다."
    )
    ApiResponse<Void> saveBestContents();




    }
