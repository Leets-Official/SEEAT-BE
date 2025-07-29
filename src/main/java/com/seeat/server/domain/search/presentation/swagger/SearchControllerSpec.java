package com.seeat.server.domain.search.presentation.swagger;

import com.seeat.server.domain.search.application.dto.response.SearchResponse;
import com.seeat.server.domain.user.domain.entity.User;
import com.seeat.server.global.response.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import java.util.List;

@Tag(name = "검색 API", description = "검색을 위한 API 입니다.")
public interface SearchControllerSpec {

    /**
     * 최근 검색 리스트 조회 API
     *
     * @param user 유저
     * @return 최근 검색 리스트
     */
    @Operation(
            summary = "최근 검색 리스트 조회",
            description = "사용자 ID로 검색 리스트를 최신별로 조회합니다."
    )
    @GetMapping
    ApiResponse<List<SearchResponse>> getSearchList(
            @Parameter(hidden = true)
            @AuthenticationPrincipal User user);

    /**
     * 최근 검색어 하나 삭제 API
     *
     * @param searchId 검색어 ID
     * @param user 유저
     * @return ok 응답값
     */
    @Operation(
            summary = "검색어 기록 삭제",
            description = "검색어 하나의 기록을 삭제합니다."
    )
    @DeleteMapping("/{searchId}")
    ApiResponse<Void> deleteSearch(
            @PathVariable Long searchId,
            @Parameter(hidden = true)
            @AuthenticationPrincipal User user);

}
