package com.seeat.server.domain.hashtag.presentation;

import com.seeat.server.domain.hashtag.application.dto.response.AuditoriumHashTagResponse;
import com.seeat.server.domain.hashtag.application.dto.response.HashTagResponse;
import com.seeat.server.domain.hashtag.application.usecase.HashTagUseCase;
import com.seeat.server.domain.hashtag.application.usecase.ReviewHashTagUseCase;
import com.seeat.server.domain.hashtag.presentation.swagger.HashTagControllerSpec;
import com.seeat.server.global.response.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import java.util.List;

@RestController
@RequestMapping("/api/v1/hashtag")
@RequiredArgsConstructor
public class HashTagController implements HashTagControllerSpec {

    private final HashTagUseCase hashTagService;

    private final ReviewHashTagUseCase reviewHashTagService;

    /**
     * 해시태그 조회 API
     */
    @GetMapping()
    public ApiResponse<List<HashTagResponse>> getAll(){

        /// 서비스
        List<HashTagResponse> hashTagResponses = hashTagService.loadAllHashTags();

        /// 리턴
        return ApiResponse.ok(hashTagResponses);

    };

    /**
     * 특정 상영관에서 많이 사용된 해시태그를 조회하는 API
     * @param auditoriumId  상영관 ID
     */
    @GetMapping("/{auditoriumId}")
    public ApiResponse<List<AuditoriumHashTagResponse>> getAuditoriumHashTagsById(
            @PathVariable String auditoriumId) {

        /// 서비스
        List<AuditoriumHashTagResponse> responses = reviewHashTagService.loadReviewHashTagsByAuditoriumId(auditoriumId);

        /// 리턴
        return ApiResponse.ok(responses);
    }

}
