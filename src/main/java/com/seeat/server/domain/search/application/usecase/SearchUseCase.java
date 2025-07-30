package com.seeat.server.domain.search.application.usecase;

import com.seeat.server.domain.search.application.dto.request.ReviewSearchCondition;
import com.seeat.server.domain.search.application.dto.response.ReviewSearchResponse;
import com.seeat.server.domain.search.application.dto.response.SearchResponse;
import com.seeat.server.domain.search.domain.entity.Search;
import com.seeat.server.domain.search.domain.entity.SortType;
import com.seeat.server.domain.user.domain.entity.User;
import com.seeat.server.global.response.pageable.PageRequest;
import com.seeat.server.global.response.pageable.SliceResponse;

import java.util.List;

public interface SearchUseCase {

    // 최근 검색어 리스트 조회
    List<SearchResponse> getSearchList(Long userId);

    // 검색어 삭제
    void deleteSearch(Long searchId, Long userId);

    // 인기순, 평점순, 최신순 조회
    SliceResponse<ReviewSearchResponse> getReviewList(ReviewSearchCondition request,
                                                      Long userId, PageRequest pageRequest);



    /// 공통 에러처리 함수
    Search getSearch(Long searchId);

}
