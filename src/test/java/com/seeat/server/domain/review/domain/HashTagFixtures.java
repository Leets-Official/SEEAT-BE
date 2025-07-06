package com.seeat.server.domain.review.domain;

import com.seeat.server.domain.review.domain.entity.HashTag;
import com.seeat.server.domain.review.domain.entity.HashTagType;

public class HashTagFixtures {

    public static HashTag createHashTag(HashTagType type, String name) {
        return HashTag.builder()
                .name(name)
                .type(type)
                .build();
    }

}
