package com.seeat.server.global.service;

import com.seeat.server.domain.search.application.dto.response.SearchResponse;
import com.seeat.server.global.util.RedisKeyUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.ListOperations;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.List;

@Service
@RequiredArgsConstructor
public class RecentSearchRedisService {

    private final RedisTemplate<String, String> redisTemplate;
    private static final int MAX_RECENT_SEARCH_SIZE = 10;
    private static final Duration EXPIRE_DURATION = Duration.ofDays(7);

    /**
     * 비회원 최근검색어 추가
     * @param guestToken 비회원 식별 토큰
     * @param keyword 검색어
     */
    public void addRecentSearch(String guestToken, String keyword) {
        String redisKey = RedisKeyUtil.getGuestRecentSearchKey(guestToken);

        ListOperations<String, String> listOps = redisTemplate.opsForList();

        // 중복 제거
        listOps.remove(redisKey, 0, keyword);

        // 왼쪽(앞)에 삽입
        listOps.leftPush(redisKey, keyword);

        // 최대 저장 개수 유지
        listOps.trim(redisKey, 0, MAX_RECENT_SEARCH_SIZE - 1);

        // TTL 설정
        redisTemplate.expire(redisKey, EXPIRE_DURATION);
    }

    /**
     * 비회원 최근 검색어 삭제
     *
     * @param guestToken 게스트 토큰
     * @param keyword 검색어
     */
    public void deleteGuestSearch(String guestToken, String keyword) {

        String redisKey = RedisKeyUtil.getGuestRecentSearchKey(guestToken);
        ListOperations<String, String> listOps = redisTemplate.opsForList();

        // 삭제
        listOps.remove(redisKey, 1, keyword);
    }

    /**
     * 비회원 최근검색어 조회 응답 구조로 변환
     *
     * @param guestToken 게스트 토큰
     * @return List<SearchResponse> 응답
     */
    public List<SearchResponse> getGuestSearchList(String guestToken) {

        List<String> recentSearches = getRecentSearches(guestToken);

        return SearchResponse.fromStrings(recentSearches);
    }

    /**
     * 비회원 최근검색어 조회
     *
     * @param guestToken 비회원 식별 토큰
     * @return 최근검색어 리스트
     */
    public List<String> getRecentSearches(String guestToken) {
        String redisKey = RedisKeyUtil.getGuestRecentSearchKey(guestToken);

        ListOperations<String, String> listOps = redisTemplate.opsForList();

        // 조회
        return listOps.range(redisKey, 0, -1);
    }
}
