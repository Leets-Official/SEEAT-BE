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
            @AuthenticationPrincipal User user,
            @RequestHeader(value = "X-Guest-Token", required = false) String guestToken){

        // 최근 검색어 리스트 조회
        List<SearchResponse> responses;

        if (guestToken != null) {
            // 비회원
            responses = service.getGuestSearchList(guestToken);
        } else {
            // 회원
            responses = service.getSearchList(user.getId());
        }

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
    @DeleteMapping
    public ApiResponse<Void> deleteSearch(
            @RequestParam(required = false) Long searchId,
            @RequestParam(required = false) String keyword,
            @RequestHeader(value = "X-Guest-Token", required = false) String guestToken,
            @AuthenticationPrincipal User user){

        if (guestToken != null) {
            // 비회원 - 삭제
            service.deleteGuestSearch(guestToken, keyword);
        } else {
            // 회원 - 삭제
            service.deleteSearch(searchId, user.getId());
        }

        // 삭제 성공 응답
        return ApiResponse.deleted();
    }

    /**
     * 리뷰 필터 검색 조회를 합니다.
     *
     * @param request 필터 조건 DTO
     * @param user 유저
     * @param pageRequest 페이징
     * @return 검색 기반 리뷰 DTO 응답
     */
    @GetMapping("/reviews")
    public ApiResponse<SliceResponse<ReviewSearchResponse>> getReviewList(
            @ModelAttribute ReviewSearchCondition request,
            @AuthenticationPrincipal User user,
            @RequestHeader(value = "X-Guest-Token", required = false) String guestToken,
            PageRequest pageRequest){

        // user null 처리
        Long userId = user != null ? user.getId() : null;

        // 조회 서비스 호출
        SliceResponse<ReviewSearchResponse> responses = service.getReviewList(request, guestToken, userId, pageRequest);

        // 응답값
        return ApiResponse.ok(responses);
    }


}
