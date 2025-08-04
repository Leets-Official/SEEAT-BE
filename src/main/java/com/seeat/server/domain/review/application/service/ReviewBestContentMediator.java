package com.seeat.server.domain.review.application.service;

import com.seeat.server.domain.best.application.dto.response.BestReviewListResponse;
import com.seeat.server.domain.hashtag.application.service.ReviewHashTagService;
import com.seeat.server.domain.review.application.usecase.ReviewBestContentMediatorUseCase;
import com.seeat.server.domain.hashtag.domain.entity.ReviewHashTag;
import com.seeat.server.domain.review.application.usecase.ReviewSeatUseCase;
import com.seeat.server.domain.review.domain.entity.Review;
import com.seeat.server.domain.review.domain.repository.ReviewRepository;
import com.seeat.server.domain.review.domain.repository.dto.ReviewWithLikeCount;
import com.seeat.server.domain.theater.domain.entity.Seat;
import com.seeat.server.global.response.pageable.PageRequest;
import com.seeat.server.global.response.pageable.SliceResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.data.domain.SliceImpl;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static com.seeat.server.global.response.pageable.PageUtil.getPageable;

@Service
@Transactional
@RequiredArgsConstructor
public class ReviewBestContentMediator implements ReviewBestContentMediatorUseCase {


    private final ReviewRepository repository;
    private final ReviewHashTagService hashTagService;

    private final ReviewSeatUseCase seatService;
    /**
     * 베스트 리뷰 조회를 위해 사용되는 함수
     * @param pageRequest   페이지
     */
    @Override
    public SliceResponse<BestReviewListResponse> getBestReviews(PageRequest pageRequest) {

        /// Pageable 처리
        Pageable pageable = getPageable(pageRequest);

        Slice<ReviewWithLikeCount> reviews = repository.findBestReviews(pageable);

        /// DTO 변경
        // 리뷰 ID 목록 추출
        List<Long> reviewIds = getLongs(reviews);

        // 리뷰 ID로 해시태그 한 번에 조회 (IN 쿼리)
        List<BestReviewListResponse> result = getBestReviewListResponses(reviewIds, reviews);

        /// Slice 객체 처리
        SliceImpl<BestReviewListResponse> slice = new SliceImpl<>(result, reviews.getPageable(), reviews.hasNext());
        return SliceResponse.from(slice);
    }


    /**
     * 리뷰의 Id를 바탕으로 BestReview DTO 변경  로직
     * @param reviewIds ID 추출 목록
     * @param reviews Page 처리를 한 리뷰 엔티티
     */
    /**
     * 리뷰의 Id를 바탕으로 BestReview DTO 변경  로직
     * @param reviewIds ID 추출 목록
     * @param reviews Page 처리를 한 리뷰 엔티티
     */
    private List<BestReviewListResponse> getBestReviewListResponses(List<Long> reviewIds, Slice<ReviewWithLikeCount> reviews) {

        // 1. 해시태그 한 번에 조회 (IN 쿼리)
        List<ReviewHashTag> allHashTags = hashTagService.getReviewHashTagByReviews(reviewIds);

        // 2. 리뷰 ID별 해시태그 매핑
        Map<Long, List<ReviewHashTag>> mapping = allHashTags.stream()
                .collect(Collectors.groupingBy(ht -> ht.getReview().getId()));

        // 3. 리뷰 ID별 좌석들 한 번에 조회
        Map<Long, List<Seat>> reviewSeatMap = seatService.loadSeatsByReviewIds(reviewIds);

        // 4. DTO 변환
        return reviews.stream()
                .map(reviewWithLikeCount -> {
                    Review review = reviewWithLikeCount.getReview();
                    Long reviewId = review.getId();
                    List<ReviewHashTag> tags = mapping.getOrDefault(reviewId, List.of());
                    List<Seat> seats = reviewSeatMap.getOrDefault(reviewId, List.of());
                    return BestReviewListResponse.from(
                            review,
                            tags,
                            reviewWithLikeCount.getLikeCount(),
                            seats
                    );
                })
                .toList();
    }


    /**
     * 리뷰의 Id를 얻기 위한 공통 로직
     * @param reviews ID를 추출할 리뷰 목록
     */
    private List<Long> getLongs(Slice<ReviewWithLikeCount> reviews) {
        return reviews.getContent().stream()
                .map(r -> r.getReview().getId())
                .toList();
    }

}
