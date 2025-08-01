package com.seeat.server.domain.review.presentation.swagger;

import com.seeat.server.domain.review.application.dto.response.AuditoriumHashTagResponse;
import com.seeat.server.domain.review.application.dto.response.HashTagResponse;
import com.seeat.server.global.response.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.PathVariable;

import java.util.List;

@Tag(name = "해시태그 API", description = "해시태그 목록을 가져오는 API 입니다.")
public interface HashTagControllerSpec {

    /**
     * 해시태그 조회 API
     */
    @Operation(
            summary = "해시태그 목록 API",
            description = "해시태그 목록을 가져오는 API입니다."
    )
    ApiResponse<List<HashTagResponse>> getAll();


    /**
     * 특정 상영관에서 많이 사용된 해시태그를 조회하는 API
     *
     * @param auditoriumId 상영관 ID
     */
    @Operation(
            summary = "상영관 해시태그 목록 API",
            description = "상영관에서 자주 사용된 해시태그 목록을 가져오는 API입니다."
    )
    ApiResponse<List<AuditoriumHashTagResponse>> getAuditoriumHashTagsById(

            @Parameter(example = "13018")
            @PathVariable String auditoriumId);

    }
