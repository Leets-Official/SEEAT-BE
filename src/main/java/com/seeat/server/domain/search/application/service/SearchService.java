package com.seeat.server.domain.search.application.service;

import com.seeat.server.domain.search.application.dto.response.SearchResponse;
import com.seeat.server.domain.search.application.usecase.SearchUseCase;
import com.seeat.server.domain.search.domain.entity.Search;
import com.seeat.server.domain.search.domain.repository.SearchRepository;
import com.seeat.server.domain.user.application.usecase.UserUseCase;
import com.seeat.server.domain.user.domain.entity.User;
import com.seeat.server.domain.user.domain.repository.UserSearchRepository;
import com.seeat.server.global.response.ErrorCode;
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
    private final UserSearchRepository userSearchRepository;

    /**
     * 최근 검색 리스트 조회
     * @param userId 유저 Id
     * @return 최근 검색 리스트 DTO
     */
    @Override
    public List<SearchResponse> getSearchList(Long userId){

        // 유저 예외
        userService.getUser(userId);

        // searchList 조회
        List<Search> searches = userSearchRepository.findSearchListByUserId(userId);

        return SearchResponse.from(searches);
    }

    @Override
    public void deleteSearch(Long searchId, Long userId){

        // 유저 예외
        User user = userService.getUser(userId);

        // Search 예외
        Search search = getSearch(searchId);

        // 삭제 (userSearch 지운 후 searchId로 지움)
        userSearchRepository.deleteByUserAndSearch(user, search);
        repository.deleteById(searchId);
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
