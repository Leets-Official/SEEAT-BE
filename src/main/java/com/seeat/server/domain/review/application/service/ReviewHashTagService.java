package com.seeat.server.domain.review.application.service;

import com.seeat.server.domain.review.application.dto.response.AuditoriumHashTagResponse;
import com.seeat.server.domain.review.application.usecase.ReviewHashTagUseCase;
import com.seeat.server.domain.review.domain.entity.HashTag;
import com.seeat.server.domain.review.domain.entity.HashTagType;
import com.seeat.server.domain.review.domain.entity.Review;
import com.seeat.server.domain.review.domain.entity.ReviewHashTag;
import com.seeat.server.domain.review.domain.repository.HashTagRepository;
import com.seeat.server.domain.review.domain.repository.ReviewHashTagRepository;
import com.seeat.server.domain.review.domain.repository.dto.ReviewHashTagWithCount;
import com.seeat.server.domain.theater.application.usecase.TheaterUseCase;
import com.seeat.server.domain.theater.domain.entity.Auditorium;
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
    private final TheaterUseCase theaterService;

    // ========================
    //  저장 함수
    // ========================

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

    // ========================
    //  조회 함수
    // ========================

    /**
     * 상영관 해시태그 서비스에서 사용할, 상영관에 따른 리뷰 목록 조회를 위한 외부 서비스 전용 함수
     * @param auditoriumId  상영관 ID
     */
    @Override
    public List<AuditoriumHashTagResponse> loadReviewHashTagsByAuditoriumId(String auditoriumId) {

        /// 상영관 존재 예외처리는 해당 서비스에서 진행
        Auditorium auditorium = theaterService.getAuditorium(auditoriumId);

        /// DB 조회
        List<ReviewHashTagWithCount> withCounts = repository.findByAuditorium_Id(auditorium.getId());

        return AuditoriumHashTagResponse.from(withCounts);
    }

    // ========================
    //  삭제 함수
    // ========================

    /**
     * 외부 의존성
     * @param reviewId  수정 또는 삭제하는 리뷰
     */
    @Override
    public void deleteReviewHashTagByReviewId(Long reviewId) {

        /// 리뷰ID 바탕으로 삭제 구현
        repository.deleteByReviewId(reviewId);

    }

    // ========================
    //  외부 함수
    // ========================

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

    /**
     * 리뷰 서비스에서 ID를 바탕으로 해시태그를 조회를 위해 사용할 로직
     * fetch join을 사용하여 N+1 문제를 방지하며, ReviewHashTag와 HashTag를 함께 로딩합니다.
     *
     * @param reviewIds 해시태그를 조회할 리뷰 IDs
     * @return List<ReviewHashTag> 응답
     */
    @Override
    public List<ReviewHashTag> loadReviewHashTagsByReviewIds(List<Long> reviewIds){

        return repository.findWithHashTagByReview_Ids(reviewIds);
    }


    // ========================
    //  공통 함수
    // ========================

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
