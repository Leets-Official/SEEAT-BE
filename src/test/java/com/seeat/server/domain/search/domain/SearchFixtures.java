package com.seeat.server.domain.search.domain;

import com.seeat.server.domain.search.domain.entity.Search;

/**
 * [ 검색 도메인을 생성해두는 Fixtures ]입니다.
 * - 테스트에 사용할 도메인들을 미리 정의해두어, 테스트의 가독성을 높여주도록 수행합니다.
 */

public class SearchFixtures {

    public static Search createSearch(String keyword) {
        return Search.builder()
                .content(keyword)
                .build();
    }

    public static Search createSearch() {
        return Search.builder()
                .content("test1")
                .build();
    }
}
