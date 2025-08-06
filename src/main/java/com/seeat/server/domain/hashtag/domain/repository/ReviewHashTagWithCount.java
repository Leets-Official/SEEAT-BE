package com.seeat.server.domain.hashtag.domain.repository;

import com.seeat.server.domain.hashtag.domain.entity.HashTag;

public interface ReviewHashTagWithCount {

    HashTag getHashTag();

    Long getCount();
}
