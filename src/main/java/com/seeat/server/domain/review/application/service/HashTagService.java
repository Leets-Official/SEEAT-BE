package com.seeat.server.domain.review.application.service;

import com.seeat.server.domain.review.application.dto.response.HashTagResponse;
import com.seeat.server.domain.review.application.usecase.HashTagUseCase;
import com.seeat.server.domain.review.application.usecase.ReviewHashTagUseCase;
import com.seeat.server.domain.review.domain.entity.HashTag;
import com.seeat.server.domain.review.domain.entity.Review;
import com.seeat.server.domain.review.domain.entity.ReviewHashTag;
import com.seeat.server.domain.review.domain.repository.HashTagRepository;
import com.seeat.server.domain.review.domain.repository.ReviewHashTagRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@Transactional
@RequiredArgsConstructor
public class HashTagService implements HashTagUseCase {

    private final HashTagRepository repository;

    // 외부 의존성
    private final ReviewHashTagUseCase reviewHashTagUseCase;
    private final ReviewHashTagRepository reviewHashTagRepository;

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

    @Override
    public List<List<String>> getHashTagsForReviews(List<Review> reviews){
        // 리뷰 리스트
        List<Long> reviewIds = reviews.stream()
                .map(Review::getId)
                .toList();

        // 해시태그 조회
        List<ReviewHashTag> reviewHashTags = reviewHashTagUseCase.loadReviewHashTagsByReviewIds(reviewIds);

        // 리뷰 Id별 해시태그 모으기
        Map<Long, List<String>> reviewIdToTags = reviewHashTags.stream()
                .collect(Collectors.groupingBy(rht -> rht.getReview().getId(),
                        Collectors.mapping(rht -> rht.getHashTag().getName(), Collectors.toList())));

        return reviews.stream()
                .map(r -> reviewIdToTags.getOrDefault(r.getId(), Collections.emptyList()))
                .collect(Collectors.toList());
    }


}
