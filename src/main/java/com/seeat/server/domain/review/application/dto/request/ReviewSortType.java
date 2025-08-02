package com.seeat.server.domain.review.application.dto.request;

import com.fasterxml.jackson.annotation.JsonValue;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * 리뷰 정렬 기준을 정의하는 enum 입니다.
 */
@Getter
@RequiredArgsConstructor
public enum ReviewSortType {
    LATEST("latest"),
    LIKES("likes"),
    RATING_DESC("ratingDesc"),
    RATING_ASC("ratingAsc");

    private final String value;

    @JsonValue
    public String getValue() {
        return value;
    }
}
