package com.seeat.server.domain.search.application.service;

import com.seeat.server.domain.search.application.dto.response.SearchResponse;
import com.seeat.server.domain.search.application.usecase.RecentSearchUserCase;
import com.seeat.server.domain.search.domain.SearchFixtures;
import com.seeat.server.domain.search.domain.entity.Search;
import com.seeat.server.domain.search.domain.repository.SearchRepository;
import com.seeat.server.domain.user.domain.UserFixtures;
import com.seeat.server.domain.user.domain.UserSearchFixtures;
import com.seeat.server.domain.user.domain.entity.User;
import com.seeat.server.domain.user.domain.repository.UserRepository;
import com.seeat.server.domain.user.domain.repository.UserSearchRepository;
import com.seeat.server.global.service.RecentSearchRedisService;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
public class RecentSearchServiceTest {

    @Autowired
    private RecentSearchUserCase sut;

    @Autowired
    private SearchRepository repository;

    @Autowired
    private RecentSearchRedisService recentSearchRedisService;

    @Autowired
    private RedisTemplate<String, String> redisTemplate;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private UserSearchRepository userSearchRepository;

    @BeforeEach
    void clearRedis() {
        redisTemplate.getConnectionFactory().getConnection().flushAll();
    }

    @Nested
    @DisplayName("최근 검색 리스트 조회")
    class GetRecentSearchList {

        @Test
        @DisplayName("회원 최근 검색 리스트 조회 성공")
        void getSearchList_AsUser_Success() {
            // given
            User user = userRepository.save(UserFixtures.createUser());

            Search search1 = repository.save(SearchFixtures.createSearch());
            Search search2 = repository.save(SearchFixtures.createSearch("test2"));

            userSearchRepository.save(UserSearchFixtures.createUserSearch(user, search1));
            userSearchRepository.save(UserSearchFixtures.createUserSearch(user, search2));

            // when
            List<SearchResponse> responses = sut.getSearchList(user.getId());

            // then
            assertThat(responses)
                    .hasSize(2)
                    .extracting(SearchResponse::content)
                    .containsExactlyInAnyOrder(search1.getContent(), search2.getContent());

        }

        @Test
        @DisplayName("비회원 최근 검색 리스트 조회 성공")
        void getGuestSearchList_AsGuest_Success() {
            // given
            String guestToken = "guest-1234";
            String keyword1 = "test1";
            String keyword2 = "test2";

            recentSearchRedisService.addRecentSearch(guestToken, keyword1);
            recentSearchRedisService.addRecentSearch(guestToken, keyword2);

            // when
            List<SearchResponse> responses = recentSearchRedisService.getGuestSearchList(guestToken);

            // then
            assertThat(responses)
                    .hasSize(2)
                    .extracting(SearchResponse::content)
                    .containsExactlyInAnyOrder(keyword1, keyword2);
        }
    }

    @Nested
    @DisplayName("최근 검색어 삭제")
    class DeleteRecentSearch {

        @Test
        @DisplayName("회원 최근 검색어 삭제 성공")
        void deleteSearch_AsUser_Success() {
            // given
            User user = userRepository.save(UserFixtures.createUser());
            Search search = repository.save(SearchFixtures.createSearch());
            userSearchRepository.save(UserSearchFixtures.createUserSearch(user, search));

            // when
            sut.deleteSearch(user, search);

            // then
            boolean exists = userSearchRepository.existsByUserAndSearch(user, search);
            boolean existsSearch = repository.existsById(search.getId());
            Assertions.assertThat(exists).isFalse();
            Assertions.assertThat(existsSearch).isFalse();

        }

        @Test
        @DisplayName("비회원 최근 검색어 삭제 성공")
        void deleteGuestSearch_AsGuest_Success() {
            // given
            String guestToken = "guest-1234";
            String keyword = "test1";
            recentSearchRedisService.addRecentSearch(guestToken, keyword);

            // when
            sut.deleteGuestSearch(guestToken, keyword);

            // then
            List<SearchResponse> recentSearches = recentSearchRedisService.getGuestSearchList(guestToken);
            Assertions.assertThat(recentSearches)
                    .extracting(SearchResponse::content)
                    .doesNotContain(keyword);

        }
    }

}
