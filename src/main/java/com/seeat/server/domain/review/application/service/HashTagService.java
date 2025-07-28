package com.seeat.server.domain.review.application.service;

import com.seeat.server.domain.review.application.dto.response.HashTagResponse;
import com.seeat.server.domain.review.application.usecase.HashTagUseCase;
import com.seeat.server.domain.review.domain.entity.HashTag;
import com.seeat.server.domain.review.domain.repository.HashTagRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Comparator;
import java.util.List;

@Service
@Transactional
@RequiredArgsConstructor
public class HashTagService implements HashTagUseCase {

    private final HashTagRepository repository;

    @Override
    public List<HashTagResponse> loadAllHashTags() {

        /// 가져오기
        List<HashTag> hashTags = repository.findAll();

        /// 정렬
        hashTags.sort(Comparator
                .comparing(HashTag::getType));

        /// DTO 변환
        return HashTagResponse.from(hashTags);
    }


}
