package com.seeat.server.domain.review.presentation;

import com.seeat.server.domain.review.application.dto.response.HashTagResponse;
import com.seeat.server.domain.review.application.usecase.HashTagUseCase;
import com.seeat.server.domain.review.presentation.swagger.HashTagControllerSpec;
import com.seeat.server.global.response.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import java.util.List;

@RestController
@RequestMapping("/api/v1/hashtag")
@RequiredArgsConstructor
public class HashTagController implements HashTagControllerSpec {

    private final HashTagUseCase hashTagService;

    @GetMapping()
    public ApiResponse<List<HashTagResponse>> getAll(){

        /// 서비스
        List<HashTagResponse> hashTagResponses = hashTagService.loadAllHashTags();

        /// 리턴
        return ApiResponse.ok(hashTagResponses);

    };

}
