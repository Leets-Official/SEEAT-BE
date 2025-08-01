package com.seeat.server.domain.search.application.dto.response;

import com.seeat.server.domain.search.domain.entity.Search;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;

import java.util.Collections;
import java.util.List;

@Builder
@Schema(name = "[응답][검색] 최근 검색어 조회 Response",description = "최근 검색어 조회에 대한 DTO 입니다.")
public record SearchResponse(
        @Schema(description = "최근 검색어 내용", example = "F1")
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

    public static List<SearchResponse> fromStrings(List<String> contents) {
        if (contents == null) return Collections.emptyList();
        return contents.stream()
                .map(content -> SearchResponse.builder()
                        .content(content)
                        .build())
                .toList();
    }
}
