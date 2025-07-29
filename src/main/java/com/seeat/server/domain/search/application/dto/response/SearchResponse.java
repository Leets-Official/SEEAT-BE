package com.seeat.server.domain.search.application.dto.response;

import com.seeat.server.domain.search.domain.entity.Search;
import lombok.Builder;

import java.util.List;

@Builder
public record SearchResponse(
        String content

) {

    // 정적 메소드
    public static List<SearchResponse> from(List<Search> searches) {
        return searches.stream()
                .map(search -> SearchResponse.builder()
                        .content(search.getContent())
                        .build())
                .toList();
    }
}
