package com.seeat.server.domain.search.presentation;

import com.seeat.server.domain.search.application.dto.request.ReviewSearchCondition;
import com.seeat.server.domain.search.application.dto.response.ReviewSearchResponse;
import com.seeat.server.domain.search.application.dto.response.SearchResponse;
import com.seeat.server.domain.search.application.usecase.SearchUseCase;
import com.seeat.server.domain.search.presentation.swagger.SearchControllerSpec;
import com.seeat.server.domain.user.domain.entity.User;
import com.seeat.server.global.response.ApiResponse;
import com.seeat.server.global.response.pageable.PageRequest;
import com.seeat.server.global.response.pageable.SliceResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

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
    public ApiResponse<List<SearchResponse>> getSearchList(
            @AuthenticationPrincipal User user){

        // 최근 검색어 리스트 조회
        List<SearchResponse> responses = service.getSearchList(user.getId());

        // 리턴
        return ApiResponse.ok(responses);
    }

    /**
     * 최근 검색어 햐나를 삭제합니다.
     *
     * @param searchId 검색어 ID
     * @param user Jwt 기반 SecurityContext 저장되어있는 유저
     * @return 200 ok 응답
     */
    @DeleteMapping("/{searchId}")
    public ApiResponse<Void> deleteSearch(
            @PathVariable Long searchId,
            @AuthenticationPrincipal User user){

        // 삭제
        service.deleteSearch(searchId, user.getId());

        // 200 리턴
        return ApiResponse.ok(null);
    }

    @GetMapping("/reviews")
    public ApiResponse<SliceResponse<ReviewSearchResponse>> getReviewList(
            @ModelAttribute ReviewSearchCondition request,
            @AuthenticationPrincipal User user,
            PageRequest pageRequest){

        // 조회 서비스 호출
        SliceResponse<ReviewSearchResponse> responses = service.getReviewList(request, user.getId(), pageRequest);

        // 응답값
        return ApiResponse.ok(responses);
    }


}
