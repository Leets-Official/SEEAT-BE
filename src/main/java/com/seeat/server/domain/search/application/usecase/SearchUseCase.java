package com.seeat.server.domain.search.application.usecase;

import com.seeat.server.domain.search.application.dto.response.SearchResponse;

import java.util.List;

public interface SearchUseCase {

    // 최근 검색어 리스트 조회
    List<SearchResponse> getSearchList(Long userId);

}
