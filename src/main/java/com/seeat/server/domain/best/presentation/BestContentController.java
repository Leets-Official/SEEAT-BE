package com.seeat.server.domain.best.presentation;

import com.seeat.server.domain.best.application.dto.response.BestReviewListResponse;
import com.seeat.server.domain.best.application.usecase.BestContentUseCase;
import com.seeat.server.domain.best.presentation.swagger.BestContentControllerSpec;
import com.seeat.server.domain.best.application.dto.response.BestAuditoriumListResponse;
import com.seeat.server.global.response.ApiResponse;
import com.seeat.server.global.response.pageable.PageRequest;
import com.seeat.server.global.response.pageable.SliceResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/home")
public class BestContentController implements BestContentControllerSpec {

    private final BestContentUseCase service;

    /**
     * 베스트 후기 조회 API
     */

    @GetMapping("/reviews")
    public ApiResponse<SliceResponse<BestReviewListResponse>> getBestReviews(
            PageRequest pageRequest) {

        /// 서비스
        SliceResponse<BestReviewListResponse> response = service.loadBestReviews(pageRequest);

        /// 리턴
        return ApiResponse.ok(response);
    }

    /**
     * 베스트 상영관 조회 API
     */
    @GetMapping("/auditoriums")
    public ApiResponse<SliceResponse<BestAuditoriumListResponse>> getBestAuditoriums(
            PageRequest pageRequest
    ) {

        ///서비스 호출
        SliceResponse<BestAuditoriumListResponse> response = service.loadBestTheaters(pageRequest);

        /// 리턴
        return ApiResponse.ok(response);

    }

    /**
     * 인기 리뷰/상영관 수동 최신화 API
     */
    @PostMapping()
    public ApiResponse<Void> saveBestContents(){

        /// 서비스
        service.saveBestContents();

        /// 리턴
        return ApiResponse.created();
    }
}
