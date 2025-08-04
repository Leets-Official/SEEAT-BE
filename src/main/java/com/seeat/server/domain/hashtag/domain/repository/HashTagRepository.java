package com.seeat.server.domain.hashtag.domain.repository;

import com.seeat.server.domain.hashtag.domain.entity.HashTag;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface HashTagRepository extends JpaRepository<HashTag, Long> {

    List<HashTag> findByIdIn(List<Long> hashTagIds);

}
