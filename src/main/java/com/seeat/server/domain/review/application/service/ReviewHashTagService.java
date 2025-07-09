package com.seeat.server.domain.review.application.service;

import com.seeat.server.domain.review.application.usecase.ReviewHashTagUseCase;
import com.seeat.server.domain.review.domain.entity.HashTag;
import com.seeat.server.domain.review.domain.entity.HashTagType;
import com.seeat.server.domain.review.domain.entity.Review;
import com.seeat.server.domain.review.domain.entity.ReviewHashTag;
import com.seeat.server.domain.review.domain.repository.HashTagRepository;
import com.seeat.server.domain.review.domain.repository.ReviewHashTagRepository;
import com.seeat.server.global.response.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Arrays;
import java.util.List;

/**
 * 리뷰 해시태그 서비스
 * - 리뷰 서비스에서 사용할 목적으로만 사용합니다.
 * - 해시태그는 각 파트별에서 1개 이상 필수로 선택 해야합니다.
 */

@Service
@Transactional
@RequiredArgsConstructor
public class ReviewHashTagService implements ReviewHashTagUseCase {

    private final ReviewHashTagRepository repository;

    /// 외부 의존성
    private final HashTagRepository hashTagRepository;

    /**
     * 리뷰 서비스에서 해시태그를 저장을 위해 사용할 로직
     * @param review   해시태그를 저장할 리뷰 엔티티
     * @param hashTagIds  리뷰에 저장할 해시태그 ID
     */
    @Override
    public void createReviewHashTag(Review review, List<Long> hashTagIds) {

        // 해시태그 조회
        List<HashTag> hashTags = hashTagRepository.findByIdIn(hashTagIds);

        // 각 파트별로 1개 이상의 해시태그를 작성해야합니다.
        boolean isValid = Arrays.stream(HashTagType.values())
                .allMatch(type -> hashTags.stream()
                                .map(HashTag::getType)
                                .filter(t -> t == type)
                                .count() >= 1);

        if (!isValid) {
            throw new IllegalArgumentException(ErrorCode.INVALID_HASHTAG.getMessage());
        }

        // DB에 저장하기
        for (HashTag hashTag : hashTags) {
            ReviewHashTag result = create(review, hashTag);
            repository.save(result);
        }

    }


    /**
     * 리뷰 서비스에서 해시태그를 조회를 위해 사용할 로직
     * @param review   해시태그를 조회할 리뷰 엔티티
     */
    @Override
    public List<ReviewHashTag> getReviewHashTagByReview(Review review) {
        return repository.findByReview(review);
    }


    /**
     * 리뷰 서비스에서 ID를 바탕으로 해시태그를 조회를 위해 사용할 로직
     * @param reviewIds   해시태그를 조회할 리뷰 IDs
     */
    @Override
    public List<ReviewHashTag> getReviewHashTagByReviews(List<Long> reviewIds) {
        return repository.findByReview_IdIn(reviewIds);
    }


    @Override
    public void deleteReviewHashTag(Long reviewHashTagId) {

    }



    /// 공통 함수 생성
    /**
     * ReviewHashTag 를 만드는 함수 입니다.
     * @param review    리뷰 엔티티
     * @param hashTag   해시태그 엔티티
     * @return ReviewHashTag 객체 생성
     */
    private ReviewHashTag create(Review review, HashTag hashTag) {
        return ReviewHashTag.builder()
                .review(review)
                .hashTag(hashTag)
                .build();
    }
}
