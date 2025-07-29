package com.seeat.server.domain.search.presentation;

import com.seeat.server.domain.search.application.dto.response.SearchResponse;
import com.seeat.server.domain.search.application.usecase.SearchUseCase;
import com.seeat.server.domain.search.presentation.swagger.SearchControllerSpec;
import com.seeat.server.domain.user.domain.entity.User;
import com.seeat.server.global.response.ApiResponse;
import io.swagger.v3.oas.annotations.Parameter;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/v1/search")
@RequiredArgsConstructor
public class SearchController implements SearchControllerSpec {

    private final SearchUseCase service;

    /**
     * 최근 검색 리스트를 조회합니다.
     *
     * @param user Jwt 기반 SecurityContext 저장되어있는 유저
     * @return 최근 검색어 리스트 DTO 응답
     */
    @GetMapping
    public ApiResponse<List<SearchResponse>> getSearchList(@AuthenticationPrincipal User user){

        // 최근 검색어 리스트 조회
        List<SearchResponse> responses = service.getSearchList(user.getId());

        // 리턴
        return ApiResponse.ok(responses);
    }


}
