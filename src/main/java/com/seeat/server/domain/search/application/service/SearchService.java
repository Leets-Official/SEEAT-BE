package com.seeat.server.domain.search.application.service;

import com.seeat.server.domain.search.application.dto.request.ReviewSearchCondition;
import com.seeat.server.domain.search.application.dto.response.ReviewSearchResponse;
import com.seeat.server.domain.search.application.dto.response.SearchResponse;
import com.seeat.server.domain.search.application.usecase.RecentSearchUserCase;
import com.seeat.server.domain.search.application.usecase.ReviewSearchUseCase;
import com.seeat.server.domain.search.application.usecase.SearchUseCase;
import com.seeat.server.domain.search.domain.entity.Search;
import com.seeat.server.domain.search.domain.repository.SearchRepository;
import com.seeat.server.domain.user.application.usecase.UserUseCase;
import com.seeat.server.domain.user.domain.entity.User;
import com.seeat.server.global.response.ErrorCode;
import com.seeat.server.global.response.pageable.PageRequest;
import com.seeat.server.global.response.pageable.SliceResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.NoSuchElementException;

@Service
@Transactional
@RequiredArgsConstructor
public class SearchService implements SearchUseCase {

    private final SearchRepository repository;

    // 외부 의존성
    private final UserUseCase userService;
    private final RecentSearchUserCase recentSearchService;
    private final ReviewSearchUseCase reviewSearchService;

    /**
     * 회원 최근 검색 리스트 조회
     * @param userId 유저 Id
     * @return 최근 검색 리스트 DTO
     */
    @Override
    public List<SearchResponse> getSearchList(Long userId){

        // 유저 예외
        userService.getUser(userId);

        return recentSearchService.getSearchList(userId);
    }

    /**
     * 비회원 최근 검색어 리스트 조회
     *
     * @param guestToken 게스트 토큰
     * @return 최근 검색 리스트 DTO
     */
    @Override
    public List<SearchResponse> getGuestSearchList(String guestToken) {

        // 토큰 없을시 에러처리
        if (guestToken == null){
            throw new IllegalArgumentException(ErrorCode.INVALID_TOKEN.getMessage());
        }

        return recentSearchService.getGuestSearchList(guestToken);
    }

    /**
     * 회원 최근 검색어 삭제
     * @param searchId 검색 Id
     * @param userId 유저 Id
     */
    @Override
    public void deleteSearch(Long searchId, Long userId){

        // 유저 예외
        User user = userService.getUser(userId);

        // Search 예외
        Search search = getSearch(searchId);

        // 삭제 (userSearch 지운 후 searchId로 지움)
        recentSearchService.deleteSearch(user, search);
    }

    /**
     * 비회원 최근 검색어 삭제
     *
     * @param guestToken 게스트 토큰
     * @param keyword 검색어
     */
    public void deleteGuestSearch(String guestToken, String keyword) {

        // 토큰 없을시 에러처리
        if (guestToken == null){
            throw new IllegalArgumentException(ErrorCode.INVALID_TOKEN.getMessage());
        }

        // 삭제
        recentSearchService.deleteGuestSearch(guestToken, keyword);
    }

    /**
     * 인기순, 평점순, 최신순 기준으로 리뷰 검색(조회)
     *
     * @param condition 필터 요청 DTD
     * @param userId 유저 Id
     * @param pageRequest 페이징
     * @return SliceResponse<ReviewSearchResponse>
     */
    public SliceResponse<ReviewSearchResponse> getReviewList(
            ReviewSearchCondition condition, String guestToken,
            Long userId, PageRequest pageRequest){

        return reviewSearchService.getReviewList(condition, guestToken, userId, pageRequest);
    }



    // 공통 함수
    /**
     * @param searchId 검색어 Id
     */
    @Override
    public Search getSearch(Long searchId){
        return repository.findById(searchId)
                .orElseThrow(() -> new NoSuchElementException(ErrorCode.NOT_SEARCH.getMessage()));
    }
}
