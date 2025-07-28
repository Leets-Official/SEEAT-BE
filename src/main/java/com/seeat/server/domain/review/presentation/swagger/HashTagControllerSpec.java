package com.seeat.server.domain.review.presentation.swagger;

import com.seeat.server.domain.review.application.dto.response.HashTagResponse;
import com.seeat.server.global.response.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;

import java.util.List;

@Tag(name = "해시태그 API", description = "해시태그 목록을 가져오는 API 입니다.")
public interface HashTagControllerSpec {

    @Operation(
            summary = "해시태그 목록 API",
            description = "해시태그 목록을 가져오는 API입니다."
    )
    ApiResponse<List<HashTagResponse>> getAll();

}
