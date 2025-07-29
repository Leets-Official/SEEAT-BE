package com.seeat.server.domain.search.application.usecase;

import com.seeat.server.domain.search.application.dto.response.SearchResponse;
import com.seeat.server.domain.search.domain.entity.Search;

import java.util.List;

public interface SearchUseCase {

    // 최근 검색어 리스트 조회
    List<SearchResponse> getSearchList(Long userId);

    // 검색어 삭제
    void deleteSearch(Long searchId, Long userId);


    // 공통 에러처리 함수
    Search getSearch(Long searchId);

}
