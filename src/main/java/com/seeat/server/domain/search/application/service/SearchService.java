package com.seeat.server.domain.search.application.service;

import com.seeat.server.domain.search.application.dto.response.SearchResponse;
import com.seeat.server.domain.search.application.usecase.SearchUseCase;
import com.seeat.server.domain.search.domain.entity.Search;
import com.seeat.server.domain.search.domain.repository.SearchRepository;
import com.seeat.server.domain.user.application.usecase.UserUseCase;
import com.seeat.server.domain.user.domain.entity.User;
import com.seeat.server.domain.user.domain.repository.UserSearchRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class SearchService implements SearchUseCase {

    private final SearchRepository repository;

    // 외부 의존성
    private final UserUseCase userService;
    private final UserSearchRepository userSearchRepository;

    public List<SearchResponse> getSearchList(Long userId){

        // 유저 예외
        User user = userService.getUser(userId);

        // searchList 조회
        List<Search> searches = userSearchRepository.findSearchListByUserId(userId);

        return SearchResponse.from(searches);
    }

}
