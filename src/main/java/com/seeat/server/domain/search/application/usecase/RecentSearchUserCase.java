package com.seeat.server.domain.search.application.usecase;

import com.seeat.server.domain.search.application.dto.response.SearchResponse;
import com.seeat.server.domain.search.domain.entity.Search;
import com.seeat.server.domain.user.domain.entity.User;

import java.util.List;

public interface RecentSearchUserCase {

    // 최근 검색어 리스트 조회 - 회원
    List<SearchResponse> getSearchList(Long userId);

    // 최근 검색어 리스트 조회 - 비회원
    List<SearchResponse> getGuestSearchList(String guestToken);

    // 검색어 삭제 - 회원
    void deleteSearch(User user, Search search);

    // 검색어 삭제 - 비회원
    void deleteGuestSearch(String guestToken, String keyword);

}
