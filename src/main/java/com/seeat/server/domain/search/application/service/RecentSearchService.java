package com.seeat.server.domain.search.application.service;

import com.seeat.server.domain.search.application.dto.response.SearchResponse;
import com.seeat.server.domain.search.application.usecase.RecentSearchUserCase;
import com.seeat.server.domain.search.domain.entity.Search;
import com.seeat.server.domain.search.domain.repository.SearchRepository;
import com.seeat.server.domain.user.domain.entity.User;
import com.seeat.server.domain.user.domain.repository.UserSearchRepository;
import com.seeat.server.global.service.RecentSearchRedisService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional
@RequiredArgsConstructor
public class RecentSearchService implements RecentSearchUserCase {

    private final SearchRepository repository;

    // 외부
    private final UserSearchRepository userSearchRepository;
    private final RecentSearchRedisService recentSearchRedisService;


    /**
     * 회원 최근 검색 리스트 조회
     * @param userId 유저 Id
     * @return 최근 검색 리스트 DTO
     */
    @Override
    public List<SearchResponse> getSearchList(Long userId){

        // searchList 조회
        List<Search> searches = userSearchRepository.findSearchListByUserId(userId);

        return SearchResponse.from(searches);
    }

    /**
     * 비회원 최근 검색어 리스트 조회
     *
     * @param guestToken 게스트 토큰
     * @return 최근 검색 리스트 DTO
     */
    @Override
    public List<SearchResponse> getGuestSearchList(String guestToken) {

        return recentSearchRedisService.getGuestSearchList(guestToken);
    }

    /**
     * 회원 최근 검색어 삭제
     *
     * @param user 유저 엔티티
     * @param search 검색 엔티티
     */
    @Override
    public void deleteSearch(User user, Search search){

        // 삭제 (userSearch 지운 후 searchId로 지움)
        userSearchRepository.deleteByUserAndSearch(user, search);
        repository.deleteById(search.getId());
    }

    /**
     * 비회원 최근 검색어 삭제
     *
     * @param guestToken 게스트 토큰
     * @param keyword 검색어
     */
    public void deleteGuestSearch(String guestToken, String keyword) {

        // 삭제
        recentSearchRedisService.deleteGuestSearch(guestToken, keyword);
    }
}
