package com.seeat.server.domain.review.application.service;

import com.seeat.server.domain.review.application.usecase.ReviewUseCase;
import com.seeat.server.domain.review.domain.entity.Review;
import com.seeat.server.domain.review.domain.entity.ReviewHashTag;
import com.seeat.server.domain.review.domain.repository.ReviewRepository;
import com.seeat.server.domain.review.application.dto.request.ReviewRequest;
import com.seeat.server.domain.review.application.dto.request.ReviewUpdateRequest;
import com.seeat.server.domain.review.application.dto.response.ReviewDetailResponse;
import com.seeat.server.domain.review.application.dto.response.ReviewListResponse;
import com.seeat.server.domain.review.domain.repository.dto.ReviewWithLikeCount;
import com.seeat.server.domain.theater.domain.entity.Seat;
import com.seeat.server.domain.theater.domain.repository.SeatRepository;
import com.seeat.server.domain.user.application.UserUseCase;
import com.seeat.server.domain.user.domain.entity.User;
import com.seeat.server.global.response.ErrorCode;
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
import java.util.NoSuchElementException;
import java.util.stream.Collectors;

import static com.seeat.server.global.response.pageable.PageUtil.getPageable;

@Service
@Transactional
@RequiredArgsConstructor
public class ReviewService implements ReviewUseCase {

    private final ReviewRepository repository;
    private final ReviewHashTagService hashTagService;

    /// 외부 의존성 처리
    private final SeatRepository seatRepository;
    private final UserUseCase userService;

    // ========================
    //  저장 함수
    // ========================
    /**
     * 리뷰 저장을 위한 로직
     * @param request 리뷰를 위한 DTO
     * @param userId  리뷰를 작성할 유저 id (@AuthenticationPrincipal)
     */
    @Override
    public Review createReview(ReviewRequest request, Long userId) {

        // 좌석 예외 처리
        Seat seat = seatRepository.findById(request.getSeatId())
                .orElseThrow(() -> new NoSuchElementException(ErrorCode.NOT_SEAT.getMessage()));

        // 유저 예외처리
        User user = userService.getUser(userId);

        // 객체 생성
        Review requestReview = Review.of(user, seat, request.getMovieTitle(), request.getRating(), request.getContent(), "thumbnail");

        // DB 내 저장
        Review review = repository.save(requestReview);

        // 리뷰 내 해시태그 생성
        hashTagService.createReviewHashTag(review, request.getHashtags());

        return review;
    }

    // ========================
    //  조회 함수
    // ========================

    /**
     * 리뷰 상세 조회를 위한 로직
     * @param reviewId 상세조회할 Id
     * @return 리뷰에 대한 상세 조회 DTO
     */
    @Override
    public ReviewDetailResponse loadReview(Long reviewId) {

        /// ReviewId를 바탕으로 조회
        ReviewWithLikeCount result = repository.findReviewAndCountById(reviewId)
                .orElseThrow(() -> new NoSuchElementException(ErrorCode.NOT_REVIEW.getMessage()));

        /// ReviewId를 바탕으로 작성한 해시태그 조회
        List<ReviewHashTag> hashTags = hashTagService.getReviewHashTagByReview(result.getReview());

        return ReviewDetailResponse.from(result.getReview(), hashTags, result.getLikeCount());
    }

    /**
     * 좌석에 따른 리뷰 목록 조회를 위한 로직
     * N+1 해결을 위해 IN 사용
     * @param seatId 좌석 Id
     * @return 리뷰에 대한 목록 조회 DTO
     */
    @Override
    public SliceResponse<ReviewListResponse> loadReviewsBySeatId(String seatId, PageRequest pageRequest) {

        // Pageable 처리
        Pageable pageable = getPageable(pageRequest);

        // DB 조회
        Slice<ReviewWithLikeCount> reviews = repository.findBySeat_Id(seatId, pageable);

        // 리뷰 ID 목록 추출
        List<Long> reviewIds = getLongs(reviews);

        // 리뷰 ID로 해시태그 한 번에 조회 (IN 쿼리)
        List<ReviewListResponse> result = getReviewListResponses(reviewIds, reviews);

        // 결과
        SliceImpl<ReviewListResponse> slice = new SliceImpl<>(result, reviews.getPageable(), reviews.hasNext());

        return SliceResponse.from(slice);
    }

    /**
     * 상영관에 따른 리뷰 목록 조회를 위한 로직
     * @param auditoriumId 상영관 Id
     * @return 리뷰에 대한 목록 조회 DTO
     */
    @Override
    public SliceResponse<ReviewListResponse> loadReviewsByAuditoriumId(String auditoriumId, PageRequest pageRequest) {

        // Pageable 처리
        Pageable pageable = getPageable(pageRequest);

        // DB 조회
        Slice<ReviewWithLikeCount> reviews = repository.findByAuditorium_Id(auditoriumId, pageable);

        // 리뷰 ID 목록 추출
        List<Long> reviewIds = getLongs(reviews);

        // 리뷰 ID로 해시태그 한 번에 조회 (IN 쿼리)
        List<ReviewListResponse> result = getReviewListResponses(reviewIds, reviews);

        // 결과
        SliceImpl<ReviewListResponse> slice = new SliceImpl<>(result, reviews.getPageable(), reviews.hasNext());

        return SliceResponse.from(slice);
    }

    /**
     * 홈 화면에서 사용할 인기 리뷰 목록 조회를 위한 로직 (무한 스크롤)
     * @param pageRequest   페이지 네이션
     */
    @Override
    public SliceResponse<ReviewListResponse> loadFavoriteReviews(PageRequest pageRequest) {

        /// Pageable 처리
        Pageable pageable = getPageable(pageRequest);

        /// 인기 있는 리뷰 검색
        Slice<ReviewWithLikeCount> reviews = repository.findAllOrderByPopularity(pageable);

        /// DTO 변환
        // 리뷰 ID 목록 추출
        List<Long> reviewIds = getLongs(reviews);

        // 리뷰 ID로 해시태그 한 번에 조회 (IN 쿼리)
        List<ReviewListResponse> result = getReviewListResponses(reviewIds, reviews);

        /// Slice 객체 처리
        SliceImpl<ReviewListResponse> slice = new SliceImpl<>(result, reviews.getPageable(), reviews.hasNext());
        return SliceResponse.from(slice);
    }


    // ========================
    //  수정 함수
    // ========================

    /**
     * 리뷰 수정을 위한 로직
     * @param request 수정을 위한 DTO
     * @param userId 수정을 원하는 유저 Id (@AuthenticationPrincipal)
     */
    @Override
    public void updateReview(ReviewUpdateRequest request, Long userId) {

    }

    // ========================
    //  삭제 함수
    // ========================

    /**
     * 리뷰 삭제를 위한 로직
     * @param reviewId 삭제를 위하는 리뷰 id
     * @param userId 삭제를 원하는 유저 Id (@AuthenticationPrincipal)
     */
    @Override
    public void deleteReview(Long reviewId, Long userId) {

    }


    // ========================
    //  공통 함수
    // ========================

    /**
     * 북마크에서 무한스크롤 조회를 위한 공통 로직
     * @param reviews   리뷰들
     */
    @Override
    public Slice<ReviewListResponse> loadReviewsForBookmark(Slice<Long> reviews) {

        // List 추출
        List<Long> reviewIds = reviews.getContent();

        List<ReviewWithLikeCount> reviewsContent = repository.findByReviewIds(reviewIds);

        // 리뷰 ID로 해시태그 한 번에 조회 (IN 쿼리)
        List<ReviewListResponse> result = getReviewListResponses(reviewIds, reviewsContent);

        // 결과
        return new SliceImpl<>(result, reviews.getPageable(), reviews.hasNext());
    }


    /**
     * 북마크에서 리뷰 조회를 위해 사용되는 공통 함수
     * @param reviewId  리뷰 ID
     */
    @Override
    public Review getReview(Long reviewId) {
        return repository.findById(reviewId)
                .orElseThrow(() -> new NoSuchElementException(ErrorCode.NOT_REVIEW.getMessage()));
    }

    /**
     * 리뷰의 Id를 얻기 위한 공통 로직
     * @param reviews ID를 추출할 리뷰 목록
     */
    private List<Long> getLongs(List<Review> reviews) {
        return reviews.stream()
                .map(Review::getId)
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

    /**
     * 리뷰의 Id를 바탕으로 DTO 변경 공통 로직
     * @param reviewIds ID 추출 목록
     * @param reviews Page 처리를 한 리뷰 엔티티
     */
    private List<ReviewListResponse> getReviewListResponses(List<Long> reviewIds, List<ReviewWithLikeCount> reviews) {

        // 추출된 리뷰 ID로 해시태그 한 번에 조회 (IN 쿼리)
        List<ReviewHashTag> allHashTags = hashTagService.getReviewHashTagByReviews(reviewIds);

        // 리뷰 ID를 바탕으로 해시태그 매핑
        Map<Long, List<ReviewHashTag>> mapping = allHashTags.stream()
                .collect(Collectors.groupingBy(ht -> ht.getReview().getId()));

        // DTO 변환
        return reviews.stream()
                .map(review -> ReviewListResponse.from(
                        review.getReview(),
                        mapping.getOrDefault(review.getReview().getId(), List.of()),
                        review.getLikeCount())
                )
                .toList();
    }

    /**
     * 리뷰의 Id를 바탕으로 DTO 변경 공통 로직
     * @param reviewIds ID 추출 목록
     * @param reviews Page 처리를 한 리뷰 엔티티
     */
    private List<ReviewListResponse> getReviewListResponses(List<Long> reviewIds, Slice<ReviewWithLikeCount> reviews) {

        // 추출된 리뷰 ID로 해시태그 한 번에 조회 (IN 쿼리)
        List<ReviewHashTag> allHashTags = hashTagService.getReviewHashTagByReviews(reviewIds);

        // 리뷰 ID를 바탕으로 해시태그 매핑
        Map<Long, List<ReviewHashTag>> mapping = allHashTags.stream()
                .collect(Collectors.groupingBy(ht -> ht.getReview().getId()));

        // DTO 변환
        return reviews.stream()
                .map(review -> ReviewListResponse.from(
                        review.getReview(),
                        mapping.getOrDefault(review.getReview().getId(), List.of()),
                        review.getLikeCount())
                )
                .toList();
    }

}

