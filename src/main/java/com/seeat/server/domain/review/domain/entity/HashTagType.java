package com.seeat.server.domain.review.domain.entity;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Getter
public enum HashTagType {
    SOUND("음향"),
    COMPANION("동반인"),
    ENVIRONMENT("관람환경");

    private final String label;

}
