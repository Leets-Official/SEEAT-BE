package com.seeat.server.domain.review.application.service;

import com.seeat.server.domain.best.application.usecase.BestContentUseCase;
import com.seeat.server.domain.image.application.usecase.ReviewImageUseCase;
import com.seeat.server.domain.review.application.dto.request.ReviewSortType;
import com.seeat.server.domain.review.application.dto.response.ReviewSeatListResponse;
import com.seeat.server.domain.hashtag.application.usecase.ReviewHashTagUseCase;
import com.seeat.server.domain.review.application.usecase.ReviewUseCase;
import com.seeat.server.domain.review.domain.entity.Review;
import com.seeat.server.domain.hashtag.domain.entity.ReviewHashTag;
import com.seeat.server.domain.image.domain.entity.ReviewImage;
import com.seeat.server.domain.review.domain.repository.ReviewRepository;
import com.seeat.server.domain.review.application.dto.request.ReviewRequest;
import com.seeat.server.domain.review.application.dto.request.ReviewUpdateRequest;
import com.seeat.server.domain.review.application.dto.response.ReviewDetailResponse;
import com.seeat.server.domain.review.application.dto.response.ReviewListResponse;
import com.seeat.server.domain.review.domain.repository.dto.ReviewWithLikeCount;
import com.seeat.server.domain.review.domain.repository.dto.SeatReviewStats;
import com.seeat.server.domain.theater.application.usecase.SeatRatingUseCase;
import com.seeat.server.domain.theater.application.usecase.TheaterUseCase;
import com.seeat.server.domain.theater.domain.entity.Auditorium;
import com.seeat.server.domain.theater.domain.entity.Seat;
import com.seeat.server.domain.user.application.usecase.UserUseCase;
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

import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

import static com.seeat.server.domain.review.application.dto.request.ReviewSortType.LATEST;
import static com.seeat.server.global.response.pageable.PageUtil.getPageable;

@Service
@Transactional
@RequiredArgsConstructor
public class ReviewService implements ReviewUseCase {

    private final ReviewRepository repository;
    private final ReviewHashTagUseCase hashTagService;

    /// 이미지 의존성 처리
    private final ReviewImageUseCase imageService;

    /// 외부 의존성 처리
    private final TheaterUseCase theaterService;
    private final UserUseCase userService;
    private final SeatRatingUseCase seatRatingService;
    private final BestContentUseCase bestContentService;

    // ========================
    //  저장 함수
    // ========================
    /**
     * 리뷰 저장을 위한 로직
     * 테스트를 위해서 반환값이 존재하는 것입니다.
     * @param request 리뷰를 위한 DTO
     * @param userId  리뷰를 작성할 유저 id (@AuthenticationPrincipal)
     */
    @Override
    public List<Review> createReview(ReviewRequest request, Long userId) throws IOException {

        /// 여러 개의 좌석도 동시 기입

        /// 좌석 예외 처리
        List<Seat> seats = theaterService.getSeat(request.getSeatIds());

        /// 유저 예외처리
        User user = userService.getUser(userId);

        List<Review> savedReviews = new ArrayList<>();

        /// 한 명의 유저가 여러개의 리뷰를 동시에 저장할 시
        String groupId = UUID.randomUUID().toString();

        for (Seat seat : seats) {
            /// 리뷰 객체 생성
            Review review = Review.of(user, seat, request.getMovieTitle(), request.getRating(), request.getContent(), request.getTitle(), groupId);

            /// DB 저장
            Review savedReview = repository.save(review);

            /// 이미지 저장
            if (request.getImageUrls() != null && !request.getImageUrls().isEmpty()) {
                String thumbnail = imageService.saveReviewImage(savedReview, request.getImageUrls()).get(0);
                savedReview.changeThumbnailUrl(thumbnail);
            }

            /// 해시태그 저장
            hashTagService.createReviewHashTag(savedReview, request.getHashtags());

            /// 좌석 평점 업데이트
            seatRatingService.saveSeatRating(savedReview, seat);

            savedReviews.add(savedReview);
        }

        return savedReviews;
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

        /// ReviewId를 바탕으로 조회 (리뷰와 좋아요 동시에 조회)
        ReviewWithLikeCount result = repository.findReviewAndCountById(reviewId)
                .orElseThrow(() -> new NoSuchElementException(ErrorCode.NOT_REVIEW.getMessage()));

        /// 리뷰
        Review review = result.getReview();

        /// 같이 작성된 좌석 조회
        List<Review> reviews = repository.findByGroupId(review.getGroupId());
        List<Seat> seats = reviews.stream()
                .map(Review::getSeat)
                .toList();

        /// ReviewId를 바탕으로 작성한 해시태그 조회
        List<ReviewHashTag> hashTags = hashTagService.getReviewHashTagByReview(review);

        /// 이미지 주소 조회
        List<ReviewImage> images = imageService.getReviewImagesByReview(review);

        return ReviewDetailResponse.from(review, hashTags, result.getLikeCount(), images, seats);
    }

    /**
     * 좌석에 따른 리뷰 목록 조회를 위한 로직
     * N+1 해결을 위해 IN 사용
     * @param seatId 좌석 Id
     * @return 리뷰에 대한 목록 조회 DTO
     */
    @Override
    public SliceResponse<ReviewSeatListResponse> loadReviewsBySeatId(
            String seatId, PageRequest pageRequest,ReviewSortType sort) {

        /// 좌석 예외처리
        Seat seat = theaterService.getSeat(seatId);

        /// 좌석의 리뷰정보 조회
        SeatReviewStats stats = repository.findSeatReviewStats(seatId);

        /// Pageable 처리
        Pageable pageable = getPageable(pageRequest);

        /// 값 초기화
        Slice<ReviewWithLikeCount> reviews;

        /// Enum 에 따른 정렬

        // 정렬 분기
        switch (sort != null ? sort : LATEST) {
            case LIKES:
                reviews = repository.findBySeat_IdOrderByLikesDesc(seat.getId(), pageable);
                break;
            case RATING_DESC:
                reviews = repository.findBySeat_IdOrderByRatingDesc(seat.getId(), pageable);
                break;
            case RATING_ASC:
                reviews = repository.findBySeat_IdOrderByRatingAsc(seat.getId(), pageable);
                break;
            default: // 최신순(기본)
                reviews = repository.findBySeat_IdOrderByLatest(seat.getId(), pageable);
        }

        // 리뷰 ID 목록 추출
        List<Long> reviewIds = getLongs(reviews);

        // 리뷰 ID로 해시태그 한 번에 조회 (IN 쿼리)
        List<ReviewListResponse> result = getReviewListResponses(reviewIds, reviews);

        // DTO로 묶기 결과
        var response = ReviewSeatListResponse.from(stats, result);

        SliceImpl<ReviewSeatListResponse> slice = new SliceImpl<>(List.of(response), reviews.getPageable(), reviews.hasNext());

        return SliceResponse.from(slice);
    }

    /**
     * 상영관에 따른 리뷰 목록 조회를 위한 로직
     *
     * @param auditoriumId 상영관 Id
     * @return 리뷰에 대한 목록 조회 DTO
     */
    @Override
    public SliceResponse<ReviewListResponse> loadReviewsByAuditoriumId(
            String auditoriumId, PageRequest pageRequest, ReviewSortType sort) {

        /// 상영관 존재 예외처리
        Auditorium auditorium = theaterService.getAuditorium(auditoriumId);

        /// Pageable 처리
        Pageable pageable = getPageable(pageRequest);

        /// 값 초기화
        Slice<ReviewWithLikeCount> reviews;

        // DB 조회
        switch (sort != null ? sort : LATEST) {
            case LIKES:
                reviews = repository.findByAuditorium_IdOrderByLikesDesc(auditorium.getId(), pageable);
                break;
            case RATING_DESC:
                reviews = repository.findByAuditorium_IdOrderByRatingDesc(auditorium.getId(), pageable);
                break;
            case RATING_ASC:
                reviews = repository.findByAuditorium_IdOrderByRatingAsc(auditorium.getId(), pageable);
                break;
            default: // 최신순(기본)
                reviews = repository.findByAuditorium_IdOrderByLatest(auditorium.getId(), pageable);
        }

        /// 리뷰 ID 목록 추출
        List<Long> reviewIds = getLongs(reviews);

        /// 리뷰 ID로 해시태그 한 번에 조회 (IN 쿼리)
        List<ReviewListResponse> result = getReviewListResponses(reviewIds, reviews);

        /// 결과
        SliceImpl<ReviewListResponse> slice = new SliceImpl<>(result, reviews.getPageable(), reviews.hasNext());

        return SliceResponse.from(slice);
    }

    // ========================
    //  수정 함수
    // ========================

    /**
     * 리뷰 수정을 위한 로직
     * @param reviewId   수정을 위한 Id
     * @param request    수정을 위한 DTO
     * @param userId     수정을 원하는 유저 Id (@AuthenticationPrincipal)
     */
    @Override
    public void updateReview(Long reviewId, ReviewUpdateRequest request, Long userId) throws IOException {

        /// 유저가 맞는지 예외처리
        User user = userService.getUser(userId);

        /// 글을 작성한 유저가 맞는지 예외처리
        Review review = getReview(reviewId, user);

        /// 도메인 로직을 통한 더티체킹 수행
        review.updateReview(request.getRating(), request.getContent(), request.getTitle());

        /// 이미지 있다면 이미지도 처리
        /// 이미지 저장
        if (request.getImages() != null && !request.getImages().isEmpty()) {

            /// 기존 이미지 삭제
            imageService.deleteReviewImage(review);

            /// 새로운 이미지 추가
            String thumbnail = imageService.saveReviewImage(review, request.getImages()).get(0);
            review.changeThumbnailUrl(thumbnail);
        }

        /// 해시태그 수정한다면 삭제후, 저장하기
        if (request.getHashtags() != null && !request.getHashtags().isEmpty()) {

            /// 기존 해시태그 삭제
            hashTagService.deleteReviewHashTagByReviewId(review.getId());

            /// 해시태그 저장하기
            hashTagService.createReviewHashTag(review, request.getHashtags());

        }


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
    public void deleteReview(Long reviewId, Long userId) throws IOException {

        /// 유저가 맞는지 예외처리
        User user = userService.getUser(userId);

        /// 글을 작성한 유저가 맞는지 예외처리
        Review review = getReview(reviewId, user);

        /// 해시태그 삭제
        hashTagService.deleteReviewHashTagByReviewId(review.getId());

        /// DB 삭제
        repository.deleteById(reviewId);

        /// 이미지 삭제
        imageService.deleteReviewImage(review);

        /// 인기 게시글이라면, 캐싱 초기화
        boolean checked = bestContentService.checkBestContentsByReviewId(reviewId);

        if (checked) {
            /// 인기 게시글을 삭제 후, 다시 초기화 (리셋)
            bestContentService.resetBestContents();
        }
    }


    // ========================
    //  외부 함수
    // ========================

    /**
     * 북마크에서 무한스크롤 조회를 위한 공통 로직
     * @param reviews   리뷰들
     */
    @Override
    public Slice<ReviewListResponse> loadReviewsForBookmark(Slice<Long> reviews) {

        /// List 추출
        List<Long> reviewIds = reviews.getContent();

        List<ReviewWithLikeCount> reviewsContent = repository.findByReviewIds(reviewIds);

        /// 리뷰 ID로 해시태그 한 번에 조회 (IN 쿼리)
        List<ReviewListResponse> result = getReviewListResponses(reviewIds, reviewsContent);

        /// 결과
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
     * 내가 작성한 리뷰 확인하기
     * @param userId        로그인한 유저 ID
     * @param pageRequest   페이지 요청
     */
    @Override
    public SliceResponse<ReviewListResponse> loadMyReviews(Long userId, PageRequest pageRequest) {

        /// Pageable 처리
        Pageable pageable = getPageable(pageRequest);

        /// 나의 리뷰 조회하기
        Slice<ReviewWithLikeCount> reviews = repository.findMyReviews(userId, pageable);

        /// DTO 변환
        /// 리뷰 ID 목록 추출
        List<Long> reviewIds = getLongs(reviews);

        /// 리뷰 ID로 해시태그 한 번에 조회 (IN 쿼리)
        List<ReviewListResponse> result = getReviewListResponses(reviewIds, reviews);

        /// Slice 객체 처리
        SliceImpl<ReviewListResponse> slice = new SliceImpl<>(result, reviews.getPageable(), reviews.hasNext());
        return SliceResponse.from(slice);

    }

    /**
     * 해당 상영관에 작성된 리뷰가 존재하는지 체크
     * @param auditoriumId  상영관 ID
     */
    @Override
    public Long countsReviewsByAuditoriumId(String auditoriumId) {

        /// 상영관 존재 예외처리
        Auditorium auditorium = theaterService.getAuditorium(auditoriumId);

        /// DB 조회
        return repository.countByAuditoriumId(auditorium.getId());
    }

    // ========================
    //  공통 함수
    // ========================
    /**
     * 내부 서비스에서 유저와 리뷰가 동일하지 확인하는 공통 함수
     * @param reviewId   리뷰 ID
     * @param user      유저
     */
    private Review getReview(Long reviewId, User user) {
        return repository.findByUserAndId(user, reviewId)
                .orElseThrow(() -> new NoSuchElementException(ErrorCode.NOT_OWN_USER_REVIEW.getMessage()));
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

        /// 추출된 리뷰 ID로 해시태그 한 번에 조회 (IN 쿼리)
        List<ReviewHashTag> allHashTags = hashTagService.getReviewHashTagByReviews(reviewIds);

        /// 리뷰 ID를 바탕으로 해시태그 매핑
        Map<Long, List<ReviewHashTag>> mapping = allHashTags.stream()
                .collect(Collectors.groupingBy(ht -> ht.getReview().getId()));

        /// DTO 변환
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

        /// 추출된 리뷰 ID로 해시태그 한 번에 조회 (IN 쿼리)
        List<ReviewHashTag> allHashTags = hashTagService.getReviewHashTagByReviews(reviewIds);

        /// 리뷰 ID를 바탕으로 해시태그 매핑
        Map<Long, List<ReviewHashTag>> mapping = allHashTags.stream()
                .collect(Collectors.groupingBy(ht -> ht.getReview().getId()));

        /// DTO 변환
        return reviews.stream()
                .map(review -> ReviewListResponse.from(
                        review.getReview(),
                        mapping.getOrDefault(review.getReview().getId(), List.of()),
                        review.getLikeCount())
                )
                .toList();
    }




}

