package com.seeat.server.domain.review.application.service;

import com.seeat.server.domain.hashtag.domain.entity.HashTag;
import com.seeat.server.domain.hashtag.domain.entity.HashTagType;
import com.seeat.server.domain.hashtag.domain.entity.ReviewHashTag;
import com.seeat.server.domain.image.domain.entity.ReviewImage;
import com.seeat.server.domain.review.application.dto.request.ReviewSortType;
import com.seeat.server.domain.review.application.dto.request.ReviewUpdateRequest;
import com.seeat.server.domain.review.application.dto.response.ReviewSeatInfoResponse;
import com.seeat.server.domain.review.application.dto.response.ReviewSeatListResponse;
import com.seeat.server.domain.review.application.usecase.ReviewLikeUseCase;
import com.seeat.server.domain.review.domain.HashTagFixtures;
import com.seeat.server.domain.review.domain.ReviewFixtures;
import com.seeat.server.domain.review.domain.entity.*;
import com.seeat.server.domain.hashtag.domain.repository.HashTagRepository;
import com.seeat.server.domain.hashtag.domain.repository.ReviewHashTagRepository;
import com.seeat.server.domain.image.domain.repository.ReviewImageRepository;
import com.seeat.server.domain.review.domain.repository.ReviewRepository;
import com.seeat.server.domain.review.application.dto.request.ReviewRequest;
import com.seeat.server.domain.review.application.dto.response.ReviewDetailResponse;
import com.seeat.server.domain.review.application.dto.response.ReviewListResponse;
import com.seeat.server.domain.review.domain.repository.ReviewSeatRepository;
import com.seeat.server.domain.theater.domain.AuditoriumFixtures;
import com.seeat.server.domain.theater.domain.SeatFixtures;
import com.seeat.server.domain.theater.domain.TheaterFixtures;
import com.seeat.server.domain.theater.domain.entity.Auditorium;
import com.seeat.server.domain.theater.domain.entity.Seat;
import com.seeat.server.domain.theater.domain.entity.Theater;
import com.seeat.server.domain.theater.domain.repository.AuditoriumRepository;
import com.seeat.server.domain.theater.domain.repository.SeatRepository;
import com.seeat.server.domain.theater.domain.repository.TheaterRepository;
import com.seeat.server.domain.user.domain.UserFixtures;
import com.seeat.server.domain.user.domain.entity.User;
import com.seeat.server.domain.user.domain.repository.UserRepository;
import com.seeat.server.global.response.ErrorCode;
import com.seeat.server.global.response.pageable.PageRequest;
import com.seeat.server.global.response.pageable.SliceResponse;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.stream.Collectors;

import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;
import static org.assertj.core.api.AssertionsForInterfaceTypes.assertThat;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

/**
 * [리뷰 서비스의 통합 테스트 클래스]입니다.
 * 리뷰 생성, 조회에 대한 happy/unhappy 테스트를 수행합니다.
 */
@SpringBootTest
@ActiveProfiles("test")
@Transactional
class ReviewServiceIntTest {

    @Autowired
    private ReviewService sut;

    @Autowired
    private ReviewRepository repository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private SeatRepository seatRepository;

    /// 미리 저장되어있어야하는 의존성 추가
    @Autowired
    private HashTagRepository hashTagRepository;

    @Autowired
    private AuditoriumRepository auditoriumRepository;

    @Autowired
    private TheaterRepository theaterRepository;

    @Autowired
    private ReviewHashTagRepository reviewHashTagRepository;

    @Autowired
    private ReviewSeatRepository reviewSeatRepository;

    /// 좋아요 의존성 추가
    @Autowired
    private ReviewLikeUseCase likeService;

    @Autowired
    private ReviewImageRepository imageRepository;

    private Seat seat1;
    private Seat seat2;
    private User user1;
    private User user2;


    private Theater theater;
    private Auditorium auditorium;
    private HashTag hashTag1;
    private HashTag hashTag2;
    private HashTag hashTag3;

    /**
     * 각 테스트 실행 전 공통으로 필요한 영화관, 상영관, 좌석, 유저 데이터를 준비합니다.
     */
    @BeforeEach
    void setUp() throws IOException {
        theater = theaterRepository.save(TheaterFixtures.createTheater());
        auditorium = auditoriumRepository.save(AuditoriumFixtures.createAuditorium(theater));
        seat1 = seatRepository.save(SeatFixtures.createSeat(auditorium));
        seat2 = seatRepository.save(SeatFixtures.createSeat2(auditorium));
        user1 = userRepository.save(UserFixtures.createUser());
        user2 = userRepository.save(UserFixtures.createUser());
        hashTag1 = hashTagRepository.save(HashTagFixtures.createHashTag(HashTagType.SOUND, "음향이 좋아요"));
        hashTag2 = hashTagRepository.save(HashTagFixtures.createHashTag(HashTagType.COMPANION, "혼자 관람했어요"));
        hashTag3 = hashTagRepository.save(HashTagFixtures.createHashTag(HashTagType.ENVIRONMENT, "좌석이 넓어요"));
    }

    @Nested
    @DisplayName("생성 테스트 [이미지 포함]")
    class CreateReview {

        /**
         * 정상적인 유저가 리뷰를 생성하는 경우를 검증합니다.
         */
        @Test
        @DisplayName("[happy] 이미지 없이 로그인한 유저 리뷰 정상 생성")
        void createReviewByUser_happy() throws IOException {
            //given
            var request = getReviewRequest(List.of(seat1, seat2), 5);

            //when
            sut.createReview(request, user1.getId());

            //then
            /// 리뷰 체크
            List<Review> reviews = repository.findAll();

            // 개수 검증
            Assertions.assertThat(reviews).hasSize(1);

            // 리뷰와 정보들이 일치하는지 검증
            Review review = reviews.get(0);

            Assertions.assertThat(review.getContent()).isEqualTo(request.getContent());
            Assertions.assertThat(review.getRating()).isEqualTo(request.getRating());

            /// 좌석이 실제로 저장되는거지
            List<ReviewSeat> seats = reviewSeatRepository.findByReview(review);
            List<String> seatIds = seats.stream()
                    .map(seat -> seat.getSeat().getId())
                    .toList();

            /// 포함하는지 체크
            Assertions.assertThat(seatIds).hasSameElementsAs(List.of(seat1.getId(), seat2.getId()));

            Assertions.assertThat(seats.get(0).getSeat().getAuditorium().getTheater().getName()).isEqualTo("Test Theater");

            /// 해시태그 체크
            List<ReviewHashTag> reviewHashTags = reviewHashTagRepository.findByReview(review);
            // 1. 개수 검증
            Assertions.assertThat(reviewHashTags).hasSize(3);

            // 2. 실제 연결된 해시태그 ID와 요청한 해시태그 ID가 일치하는지 검증
            List<Long> actualHashTagIds = reviewHashTags.stream()
                    .map(rht -> rht.getHashTag().getId())
                    .toList();
            Assertions.assertThat(actualHashTagIds)
                    .containsExactlyInAnyOrderElementsOf(request.getHashtags());
        }

        @Test
        @DisplayName("[happy] 이미지 없이 로그인한 유저 리뷰 좌석 2개에 동시 생성 정상 생성")
        void createReviewByUser_happy_multiple_seat() throws IOException {
            // given
            var request = getReviewRequest(List.of(seat1, seat2), 5);

            // when
            Review review1 = sut.createReview(request, user1.getId());

            // then
            Review savedReview = repository.findById(review1.getId()).get();

            Assertions.assertThat(savedReview).isNotNull();

            // 좌석 조회(다대다)
            List<ReviewSeat> seats = reviewSeatRepository.findByReview(savedReview);
            List<String> seatIds = seats.stream()
                    .map(rs -> rs.getSeat().getId())
                    .toList();

            Assertions.assertThat(savedReview.getContent()).isEqualTo(request.getContent());
            Assertions.assertThat(savedReview.getRating()).isEqualTo(request.getRating());
            Assertions.assertThat(seatIds).hasSameElementsAs(List.of(seat1.getId(), seat2.getId()));

            // 좌석 중 하나의 극장명으로 검증 (보통 동일 극장)
            String theaterName = seats.get(0).getSeat().getAuditorium().getTheater().getName();
            Assertions.assertThat(theaterName).isEqualTo("Test Theater");

            // 해시태그 체크
            List<ReviewHashTag> reviewHashTags = reviewHashTagRepository.findByReview(savedReview);
            Assertions.assertThat(reviewHashTags).hasSize(3);

            List<Long> actualHashTagIds = reviewHashTags.stream()
                    .map(rht -> rht.getHashTag().getId())
                    .toList();

            Assertions.assertThat(actualHashTagIds)
                    .containsExactlyInAnyOrderElementsOf(request.getHashtags());
        }



        /**
         * 정상적인 유저가 이미지 한 장을 통해 리뷰를 생성하는 경우를 검증합니다.
         */
        @Test
        @DisplayName("[happy] 로그인한 유저 이미지 1장으로 리뷰 정상 생성")
        void createReviewByUser_happy_with_imageUrls() throws IOException {
            //given
            var request = ReviewRequest.builder()
                    .seatIds(List.of(seat1.getId()))
                    .title("review title")
                    .content("test")
                    .movieTitle("ReviewTestTitle")
                    .rating(5)
                    .hashtags(List.of(hashTag1.getId(), hashTag2.getId(), hashTag3.getId()))
                    .imageUrls(List.of("file1.jpg"))
                    .build();

            //when
            sut.createReview(request, user1.getId());

            //then
            // 리뷰 체크
            List<Review> reviews = repository.findAll();

            // 1. 개수 검증
            Assertions.assertThat(reviews).hasSize(1);

            // 2. 리뷰와 정보들이 일치하는지 검증
            Review review = reviews.get(0);
            Assertions.assertThat(review.getContent()).isEqualTo(request.getContent());
            Assertions.assertThat(review.getRating()).isEqualTo(request.getRating());

            // 좌석 다대다 검증
            List<ReviewSeat> reviewSeats = reviewSeatRepository.findByReview(review);
            Assertions.assertThat(reviewSeats).hasSize(1);
            Assertions.assertThat(reviewSeats.get(0).getSeat().getId()).isEqualTo(seat1.getId());
            Assertions.assertThat(reviewSeats.get(0).getSeat().getAuditorium().getTheater().getName()).isEqualTo("Test Theater");

            // 해시태그 체크
            List<ReviewHashTag> reviewHashTags = reviewHashTagRepository.findByReview(review);
            Assertions.assertThat(reviewHashTags).hasSize(3);

            List<Long> actualHashTagIds = reviewHashTags.stream()
                    .map(rht -> rht.getHashTag().getId())
                    .toList();
            Assertions.assertThat(actualHashTagIds)
                    .containsExactlyInAnyOrderElementsOf(request.getHashtags());

            // 이미지 검증
            String thumbnailUrl = review.getThumbnailUrl();
            Assertions.assertThat(thumbnailUrl).contains("file1.jpg");
        }


        /**
         * 정상적인 유저가 이미지 3 장을 통해 리뷰를 생성하는 경우를 검증합니다.
         */
        @Test
        @DisplayName("[happy] 로그인한 유저 이미지 3장으로 리뷰 정상 생성")
        void createReviewByUser_happy_with_imageUrls_3() throws IOException {
            //given
            var request = ReviewRequest.builder()
                    .seatIds(List.of(seat1.getId()))
                    .title("review title")
                    .content("test")
                    .movieTitle("ReviewTestTitle")
                    .rating(3)
                    .hashtags(List.of(hashTag1.getId(), hashTag2.getId(), hashTag3.getId()))
                    .imageUrls(List.of("file1.jpg", "file2.png", "file3.jpeg"))
                    .build();

            //when
            sut.createReview(request, user1.getId());

            //then
            // 리뷰 체크
            List<Review> reviews = repository.findAll();

            // 1. 개수 검증
            Assertions.assertThat(reviews).hasSize(1);

            // 2. 리뷰와 정보들이 일치하는지 검증
            Review review = reviews.get(0);
            Assertions.assertThat(review.getContent()).isEqualTo(request.getContent());
            Assertions.assertThat(review.getRating()).isEqualTo(request.getRating());

            // 좌석·극장 정보의 다대다 검증
            List<ReviewSeat> reviewSeats = reviewSeatRepository.findByReview(review);
            Assertions.assertThat(reviewSeats).hasSize(1);
            Assertions.assertThat(reviewSeats.get(0).getSeat().getId()).isEqualTo(seat1.getId());
            Assertions.assertThat(reviewSeats.get(0).getSeat().getAuditorium().getTheater().getName()).isEqualTo("Test Theater");

            // 해시태그 체크
            List<ReviewHashTag> reviewHashTags = reviewHashTagRepository.findByReview(review);
            Assertions.assertThat(reviewHashTags).hasSize(3);
            List<Long> actualHashTagIds = reviewHashTags.stream()
                    .map(rht -> rht.getHashTag().getId())
                    .toList();
            Assertions.assertThat(actualHashTagIds)
                    .containsExactlyInAnyOrderElementsOf(request.getHashtags());

            // 이미지 검증
            List<ReviewImage> reviewImages = imageRepository.findByReview_Id(review.getId());
            Assertions.assertThat(reviewImages).hasSize(3);

            List<String> storedFileNames = reviewImages.stream()
                    .map(ReviewImage::getImageUrl)
                    .toList();

            assertThat(storedFileNames)
                    .anyMatch(url -> url.endsWith(".jpg"))
                    .anyMatch(url -> url.endsWith(".png"))
                    .anyMatch(url -> url.endsWith(".jpeg"));

            // 썸네일 검사
            assertThat(review.getThumbnailUrl()).isEqualTo(storedFileNames.stream().findFirst().get());
        }


        /**
         * 정상적인 유저가 이미지 6 장을 통해 에러가 발생합니다..
         */
        @Test
        @DisplayName("[unhappy] 로그인한 유저가 이미지 6장 이상 등록 시 예외 발생")
        void createReviewByUser_unhappy_with_imageUrls_6() throws IOException {
            // given
            var request = ReviewRequest.builder()
                    .seatIds(List.of(seat1.getId()))
                    .title("review title")
                    .content("test")
                    .movieTitle("ReviewTestTitle")
                    .rating(3)
                    .hashtags(List.of(hashTag1.getId(), hashTag2.getId(), hashTag3.getId()))
                    .imageUrls(List.of("file1.jpg", "file2.jpg", "file3.jpg", "file4.jpg", "file5.jpg", "file6.jpg"))
                    .build();

            // when & then
            assertThatThrownBy(() -> sut.createReview(request, user1.getId()))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessage(ErrorCode.TOO_MANY_IMAGES.getMessage());
        }




        /**
         * 존재하지 않는 유저가 리뷰를 생성할 때 예외가 발생하는지 검증합니다.
         */
        @Test
        @DisplayName("[unhappy] DB에 존재하지 않는 유저가 리뷰 생성시 예외 발생")
        void createReviewByUser_unhappy_throw_NoSuchElementException() {
            //given
            User fakeUser = UserFixtures.fakeUser();
            var request = ReviewRequest.builder()
                    .seatIds(List.of(seat1.getId()))
                    .title("review title")
                    .content("test")
                    .movieTitle("ReviewTestTitle")
                    .imageUrls(null)
                    .rating(5)
                    .hashtags(List.of(hashTag1.getId(), hashTag2.getId(), hashTag3.getId()))
                    .build();

            // when & then
            Assertions.assertThatThrownBy(() -> sut.createReview(request, fakeUser.getId()))
                    .isInstanceOf(NoSuchElementException.class)
                    .hasMessageContaining(ErrorCode.NOT_USER.getMessage());
        }

        /**
         * 유저가 리뷰를 생성할 때 해시태그를 1개 이상 선택하지 않으면 예외가 발생합니다.
         */
        @Test
        @DisplayName("[unhappy] 해시태그를 1개이상 설정하지 않고 리뷰 생성시 예외 발생")
        void createReviewByUser_unhappy_throw_IllegalArgumentException() {
            //given
            var request = ReviewRequest.builder()
                    .seatIds(List.of(seat1.getId()))
                    .title("review title")
                    .content("test")
                    .movieTitle("ReviewTestTitle")
                    .imageUrls(null)
                    .rating(5)
                    .hashtags(List.of(hashTag1.getId(), hashTag2.getId()))
                    .build();

            // when & then
            Assertions.assertThatThrownBy(() -> sut.createReview(request, user1.getId()))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining(ErrorCode.INVALID_HASHTAG.getMessage());
        }
    }

    @Nested
    @DisplayName("조회 테스트")
    class LoadReview {

        /**
         * 리뷰 상세 조회가 정상 동작하는지 검증합니다.
         */
        @Test
        @DisplayName("[happy] 리뷰 상세 조회")
        void loadReview_happy() throws IOException {
            //given
            var request = getReviewRequest(List.of(seat1, seat2), 5);

            Review review1 = sut.createReview(request, user1.getId());

            //when
            ReviewDetailResponse response = sut.loadReview(review1.getId(), null);

            //then
            Assertions.assertThat(response).isNotNull();
            Assertions.assertThat(response.title()).isEqualTo(review1.getTitle());
            Assertions.assertThat(response.content()).isEqualTo(review1.getContent());
            Assertions.assertThat(response.rating()).isEqualTo(review1.getRating());
            List<ReviewSeatInfoResponse> infoResponses = response.seatInfo();

            List<String> seatIds = infoResponses.stream()
                    .map(ReviewSeatInfoResponse::seatId)
                    .toList();

            /// 포함하는지 체크
            Assertions.assertThat(seatIds).hasSameElementsAs(List.of(seat1.getId(), seat2.getId()));

        }

        /**
         * 존재하지 않는 리뷰 조회 시 예외가 발생하는지 검증합니다.
         */
        @Test
        @DisplayName("[unhappy] 존재하지 않는 리뷰 조회")
        void loadReview_unhappy_not_id() {
            //given
            Review fakeReview = ReviewFixtures.fakeReview(user1);

            // when & then
            Assertions.assertThatThrownBy(() -> sut.loadReview(fakeReview.getId(), null))
                    .isInstanceOf(NoSuchElementException.class)
                    .hasMessageContaining(ErrorCode.NOT_REVIEW.getMessage());
        }

        /**
         * 리뷰 목록 조회가 정상 동작하는지 검증합니다.
         */
        @Test
        @DisplayName("[happy] 좌석 기반_리뷰 목록 조회")
        void loadReviews_happy_seat() throws IOException {
            //given
            String firstReview = "첫번째 리뷰입니다.";
            String secondReview = "두번째 리뷰입니다.";
            //given
            var request1 = getReviewRequest(List.of(seat1, seat2), 5, firstReview);
            var request2 = getReviewRequest(List.of(seat1, seat2), 5, secondReview);

            Review review1 = sut.createReview(request1, user1.getId());
            Review review2 = sut.createReview(request2, user2.getId());

            var pageRequest = PageRequest.builder().page(1).size(10).build();

            //when
            SliceResponse<ReviewSeatListResponse> response = sut.loadReviewsBySeatId(seat1.getId(), pageRequest, ReviewSortType.LATEST);

            //then
            List<ReviewSeatListResponse> responses = response.content();

            /// 싱글톤 리스트이기에 어차피 1개
            ReviewSeatListResponse seatListResponse = responses.get(0);

            Assertions.assertThat(seatListResponse.reviews()).hasSize(2);
            ReviewListResponse response1 = seatListResponse.reviews().get(0);
            ReviewListResponse response2 = seatListResponse.reviews().get(1);

            Assertions.assertThat(response1.content()).isEqualTo(review2.getContent());
            Assertions.assertThat(response2.content()).isEqualTo(review1.getContent());
        }


        @Test
        @DisplayName("[happy] 상영관 기반_리뷰 목록 조회")
        void loadReviews_happy_theater() throws IOException {
            // given
            String firstReview = "첫번째 리뷰입니다.";
            String secondReview = "두번째 리뷰입니다.";
            String thirdReview = "세번째 리뷰입니다.";
            String fourthReview = "네번째 리뷰입니다.";

            var request1 = getReviewRequest(List.of(seat1), 1, firstReview);
            var request2 = getReviewRequest(List.of(seat1), 2, secondReview);
            var request3 = getReviewRequest(List.of(seat1, seat2), 3, thirdReview);
            var request4 = getReviewRequest(List.of(seat2), 4, fourthReview);

            Review review1 = sut.createReview(request1, user1.getId());
            Review review2 = sut.createReview(request2, user2.getId());
            Review review3 = sut.createReview(request3, user1.getId());
            Review review4 = sut.createReview(request4, user2.getId());

            var pageRequest = PageRequest.builder().page(1).size(10).build();

            // when
            SliceResponse<ReviewListResponse> response = sut.loadReviewsByAuditoriumId(auditorium.getId(), pageRequest, ReviewSortType.LATEST);

            // then
            List<ReviewListResponse> responses = response.content();
            Assertions.assertThat(responses).hasSize(4);

            // 각 리뷰의 내용이 저장한 순서대로 반환되는지 검증
            Assertions.assertThat(responses.get(0).content()).isEqualTo(fourthReview);
            Assertions.assertThat(responses.get(1).content()).isEqualTo(thirdReview);
            Assertions.assertThat(responses.get(2).content()).isEqualTo(secondReview);
            Assertions.assertThat(responses.get(3).content()).isEqualTo(firstReview);
        }
    }

    @Nested
    @DisplayName("좋아요 반영 조회 테스트")
    class LoadReviewsByLike {

        @Test
        @DisplayName("[happy] 상세 조회에서 정상적으로 좋아요 개수가 출력")
        public void happyLoad_Detail() throws IOException {

            //given
            var request = getReviewRequest(List.of(seat1), 1);

            //when
            Review review = sut.createReview(request, user1.getId());

            likeService.reviewLike(user1.getId(), review.getId());
            likeService.reviewLike(user2.getId(), review.getId());

            //when
            ReviewDetailResponse response = sut.loadReview(review.getId(), null);

            //then
            Assertions.assertThat(response).isNotNull();
            Assertions.assertThat(response.heartCount()).isEqualTo(2L);

        }

        @Test
        @DisplayName("[happy] 여러명이 동시적으로 좋아요 출력 후, 목록 조회에서 정상적으로 좋아요 개수가 출력")
        public void happyLoad_List() throws IOException {

            // given
            PageRequest pageRequest = PageRequest.builder().page(1).size(10).build();

            var request1 = ReviewRequest.builder()
                    .seatIds(List.of(seat1.getId()))
                    .title("review title")
                    .content("test1")
                    .movieTitle("ReviewTestTitle1")
                    .imageUrls(null)
                    .rating(5)
                    .hashtags(List.of(hashTag1.getId(), hashTag2.getId(), hashTag3.getId()))
                    .build();

            var request2 = ReviewRequest.builder()
                    .seatIds(List.of(seat1.getId()))
                    .title("review title")
                    .content("test2")
                    .movieTitle("ReviewTestTitle2")
                    .imageUrls(null)
                    .rating(3)
                    .hashtags(List.of(hashTag1.getId(), hashTag2.getId(), hashTag3.getId()))
                    .build();

            Review review1 = sut.createReview(request1, user1.getId());
            Review review2 = sut.createReview(request2, user1.getId());

            // 1에게만 좋아요 누르기
            likeService.reviewLike(user1.getId(), review1.getId());
            likeService.reviewLike(user2.getId(), review1.getId());

            // when
            SliceResponse<ReviewListResponse> response = sut.loadReviewsByAuditoriumId(auditorium.getId(), pageRequest, ReviewSortType.RATING_DESC);

            // then
            List<ReviewListResponse> contents = response.content();
            Assertions.assertThat(contents).isNotNull();
            Assertions.assertThat(contents.size()).isEqualTo(2);
            Assertions.assertThat(response.content().get(0).heartCount()).isEqualTo(2L);
            Assertions.assertThat(response.content().get(1).heartCount()).isEqualTo(0);

        }
    }

    @Nested
    @DisplayName("수정 테스트")
    class UpdateReviewTest {

        @Test
        @DisplayName("[happy] 사진 제외, 리뷰의 작성자는 정상적으로 수정할 수 있습니다.")
        void update_happy() throws IOException {

            // given
            /// 요청
            var request = getReviewRequest(seat1, 4);

            /// 생성
            Review review = sut.createReview(request, user1.getId());

            /// 수정용 요청
            var newRequest = ReviewUpdateRequest.
                    builder()
                    .content("수정")
                    .rating(1)
                    .build();

            // when
            sut.updateReview(review.getId(), newRequest, user1.getId());

            // then
            var savedReview = repository.findById(review.getId()).get();

            Assertions.assertThat(savedReview).isNotNull();
            /// 새로운 값 적용 여부
            Assertions.assertThat(savedReview.getRating()).isEqualTo(1);
            Assertions.assertThat(savedReview.getContent()).isEqualTo("수정");

            /// 기존내용 변경 여부
            Assertions.assertThat(savedReview.getMovieTitle()).isEqualTo(request.getMovieTitle());

        }

        @Test
        @DisplayName("[happy] 사진 포함, 리뷰의 작성자는 정상적으로 수정할 수 있습니다.")
        void update_happy_imageUrls() throws IOException {

            // given
            /// 요청
            var request = getReviewRequest(seat1, 4);

            /// 생성
            Review review = sut.createReview(request, user1.getId());

            /// 수정용 요청
            var newRequest = ReviewUpdateRequest.
                    builder()
                    .content("수정")
                    .images(List.of("file1.jpg"))
                    .rating(1)
                    .build();

            Assertions.assertThat(review.getThumbnailUrl()).isEqualTo("thumbnailUrl");

            // when
            sut.updateReview(review.getId(), newRequest, user1.getId());

            // then
            var savedReview = repository.findById(review.getId()).get();

            Assertions.assertThat(savedReview).isNotNull();
            /// 새로운 값 적용 여부
            Assertions.assertThat(savedReview.getRating()).isEqualTo(1);
            Assertions.assertThat(savedReview.getContent()).isEqualTo("수정");

            // thumbnailUrl 검증: 빈 문자열이 아니고 .png 확장자 포함 확인
            Assertions.assertThat(savedReview.getThumbnailUrl()).isNotBlank();
            Assertions.assertThat(savedReview.getThumbnailUrl()).contains(".jpg");


            /// 기존내용 변경 여부
            Assertions.assertThat(savedReview.getMovieTitle()).isEqualTo(request.getMovieTitle());

        }

        @Test
        @DisplayName("[unhappy] 존재하지 않는 사용자는 수정할 수 없습니다.")
        void update_throws_not_users() throws IOException {

            // given
            Long attackedUserId = 99999999L;

            var request = getReviewRequest(seat1, 4);

            /// 생성
            Review review = sut.createReview(request, user1.getId());

            /// 수정용 요청
            var newRequest = ReviewUpdateRequest.
                    builder()
                    .content("수정")
                    .rating(1)
                    .build();

            // when & then
            assertThatThrownBy(() -> sut.updateReview(review.getId(), newRequest, attackedUserId))
                    .isInstanceOf(NoSuchElementException.class)
                    .hasMessage(ErrorCode.NOT_USER.getMessage());
        }

        @Test
        @DisplayName("[unhappy] 리뷰의 작성자가 아닌 사용자는 수정할 수 없습니다.")
        void update_throws_not_my_reviews() throws IOException {

            // given
            Long attackedUserId = user2.getId();

            var request = getReviewRequest(seat1, 4);

            /// 생성
            Review review = sut.createReview(request, user1.getId());

            /// 수정용 요청
            var newRequest = ReviewUpdateRequest.
                    builder()
                    .content("수정")
                    .rating(1)
                    .build();

            // when & then
            assertThatThrownBy(() -> sut.updateReview(review.getId(), newRequest, attackedUserId))
                    .isInstanceOf(NoSuchElementException.class)
                    .hasMessage(ErrorCode.NOT_OWN_USER_REVIEW.getMessage());
        }
    }

    @Nested
    @DisplayName("삭제 테스트")
    class DeleteReview {

        @Test
        @DisplayName("[happy] 리뷰의 작성자는 정상적으로 삭제할 수 있습니다.")
        void happyDelete() throws IOException {

            /// 생성
            Review review = repository.save(ReviewFixtures.createReview(user1));
            Long reviewId = review.getId();

            // when
            sut.deleteReview(review.getId(), user1.getId());

            // then
            /// 해시태그 존재하는지 체크
            List<ReviewHashTag> hashTags = reviewHashTagRepository.findByReview_Id(reviewId);
            Assertions.assertThat(hashTags).isEmpty();

            /// 이미지 존재하는지 체크
            List<ReviewImage> images = imageRepository.findByReview_Id(reviewId);
            Assertions.assertThat(images).isEmpty();
        }

        @Test
        @DisplayName("[unhappy] 존재하지 않는 사용자는 삭제할 수 없습니다.")
        void delete_throws_not_users() throws IOException {

            // given
            Long attackedUserId = 99999999L;

            var request = getReviewRequest(seat1, 4);

            /// 생성
            Review review = sut.createReview(request, user1.getId());

            // when & then
            assertThatThrownBy(() -> sut.deleteReview(review.getId(), attackedUserId))
                    .isInstanceOf(NoSuchElementException.class)
                    .hasMessage(ErrorCode.NOT_USER.getMessage());

        }

        @Test
        @DisplayName("[unhappy] 리뷰의 작성자가 아닌 사용자는 삭제할 수 없습니다.")
        void delete_throws_not_my_reviews() throws IOException {

            // given
            Long attackedUserId = user2.getId();

            var request = getReviewRequest(seat1, 4);

            /// 생성
            Review review = sut.createReview(request, user1.getId());

            // when & then
            assertThatThrownBy(() -> sut.deleteReview(review.getId(), attackedUserId))
                    .isInstanceOf(NoSuchElementException.class)
                    .hasMessage(ErrorCode.NOT_OWN_USER_REVIEW.getMessage());

        }
    }

    @Nested
    @DisplayName("좌석 기반 리뷰 목록 정렬 테스트")
    class LoadReviewsBySeatIdSortTest {

        @Test
        @DisplayName("[happy] 최신순(LATEST) 정렬")
        void loadReviewsBySeat_LATEST() throws IOException {
            //given
            sut.createReview(getReviewRequest(seat1, 1, "first review"), user1.getId());
            sut.createReview(getReviewRequest(seat1, 4, "second review"), user1.getId());
            sut.createReview(getReviewRequest(seat1, 5, "third review"), user1.getId());

            var pageRequest = PageRequest.builder().page(1).size(10).build();

            //when
            SliceResponse<ReviewSeatListResponse> response = sut.loadReviewsBySeatId(seat1.getId(), pageRequest, ReviewSortType.LATEST);

            //then : 저장 역순으로 나와야 함
            List<ReviewListResponse> result = response.content().get(0).reviews();
            Assertions.assertThat(result.get(0).content()).isEqualTo("third review");
            Assertions.assertThat(result.get(1).content()).isEqualTo("second review");
            Assertions.assertThat(result.get(2).content()).isEqualTo("first review");
        }

        @Test
        @DisplayName("[happy] 평점 내림차순(RATING_DESC) 정렬")
        void loadReviewsBySeat_RATING_DESC() throws IOException {
            //given
            sut.createReview(getReviewRequest(seat1, 3, "review 3"), user1.getId());
            sut.createReview(getReviewRequest(seat1, 5, "review 5"), user1.getId());
            sut.createReview(getReviewRequest(seat1, 1, "review 1"), user1.getId());

            var pageRequest = PageRequest.builder().page(1).size(10).build();

            //when
            SliceResponse<ReviewSeatListResponse> response = sut.loadReviewsBySeatId(seat1.getId(), pageRequest, ReviewSortType.RATING_DESC);

            //then : 5 → 3 → 1 순서
            List<ReviewListResponse> result = response.content().get(0).reviews();
            Assertions.assertThat(result.get(0).content()).isEqualTo("review 5");
            Assertions.assertThat(result.get(1).content()).isEqualTo("review 3");
            Assertions.assertThat(result.get(2).content()).isEqualTo("review 1");
        }

        @Test
        @DisplayName("[happy] 평점 오름차순(RATING_ASC) 정렬")
        void loadReviewsBySeat_RATING_ASC() throws IOException {
            //given
            sut.createReview(getReviewRequest(seat1, 2, "review 2"), user1.getId());
            sut.createReview(getReviewRequest(seat1, 4, "review 4"), user1.getId());
            sut.createReview(getReviewRequest(seat1, 3, "review 3"), user1.getId());
            var pageRequest = PageRequest.builder().page(1).size(10).build();

            //when
            SliceResponse<ReviewSeatListResponse> response = sut.loadReviewsBySeatId(seat1.getId(), pageRequest, ReviewSortType.RATING_ASC);

            //then : 2 → 3 → 4 순
            List<ReviewListResponse> result = response.content().get(0).reviews();
            Assertions.assertThat(result.get(0).content()).isEqualTo("review 2");
            Assertions.assertThat(result.get(1).content()).isEqualTo("review 3");
            Assertions.assertThat(result.get(2).content()).isEqualTo("review 4");
        }

        @Test
        @DisplayName("[happy] 좋아요 내림차순(LIKES) 정렬")
        void loadReviewsBySeat_LIKES() throws IOException {
            //given

            Review review1 = sut.createReview(getReviewRequest(seat1, 5, "review A"), user1.getId());
            Review review2 = sut.createReview(getReviewRequest(seat1, 4, "review B"), user1.getId());
            Review review3 = sut.createReview(getReviewRequest(seat1, 3, "review C"), user1.getId());

            likeService.reviewLike(user1.getId(), review2.getId());
            likeService.reviewLike(user2.getId(), review2.getId());
            likeService.reviewLike(user2.getId(), review3.getId());
            likeService.reviewLike(user1.getId(), review3.getId());
            likeService.reviewLike(user1.getId(), review1.getId());

            // r2(2명), r3(2명), r1(1명) → r2, r3, r1(동점시 저장순 or id순)
            var pageRequest = PageRequest.builder().page(1).size(10).build();

            //when
            SliceResponse<ReviewSeatListResponse> response = sut.loadReviewsBySeatId(seat1.getId(), pageRequest, ReviewSortType.LIKES);

            //then
            List<ReviewListResponse> result = response.content().get(0).reviews();
            Assertions.assertThat(result.get(0).heartCount()).isGreaterThanOrEqualTo(result.get(1).heartCount());
            Assertions.assertThat(result.get(0).heartCount()).isGreaterThanOrEqualTo(result.get(2).heartCount());
        }
    }


    @Nested
    @DisplayName("상영관 기반 리뷰 목록 정렬 테스트")
    class LoadReviewsByAuditoriumIdSortTest {

        @Test
        @DisplayName("[happy] 최신순(LATEST) 정렬")
        void loadReviewsByAuditorium_LATEST() throws IOException {

            // given
            sut.createReview(getReviewRequest(seat1, 2, "oldest"), user1.getId());
            sut.createReview(getReviewRequest(seat1, 3, "middle"), user1.getId());
            sut.createReview(getReviewRequest(seat1, 1, "latest"), user1.getId());
            var pageRequest = PageRequest.builder().page(1).size(10).build();

            // when
            SliceResponse<ReviewListResponse> response = sut.loadReviewsByAuditoriumId(auditorium.getId(), pageRequest, ReviewSortType.LATEST);

            // then
            List<ReviewListResponse> result = response.content();
            Assertions.assertThat(result.get(0).content()).isEqualTo("latest");
            Assertions.assertThat(result.get(1).content()).isEqualTo("middle");
            Assertions.assertThat(result.get(2).content()).isEqualTo("oldest");
        }

        @Test
        @DisplayName("[happy] 평점 내림차순(RATING_DESC) 정렬")
        void loadReviewsByAuditorium_RATING_DESC() throws IOException {
            // given
            sut.createReview(getReviewRequest(seat1, 2, "r1"), user1.getId());
            sut.createReview(getReviewRequest(seat1, 3, "r5"), user1.getId());
            sut.createReview(getReviewRequest(seat1, 1, "r3"), user1.getId());
            var pageRequest = PageRequest.builder().page(1).size(10).build();

            // when
            SliceResponse<ReviewListResponse> response = sut.loadReviewsByAuditoriumId(auditorium.getId(), pageRequest, ReviewSortType.RATING_DESC);

            // then
            List<ReviewListResponse> result = response.content();
            Assertions.assertThat(result.get(0).content()).isEqualTo("r5");
            Assertions.assertThat(result.get(1).content()).isEqualTo("r1");
            Assertions.assertThat(result.get(2).content()).isEqualTo("r3");
        }

        @Test
        @DisplayName("[happy] 평점 오름차순(RATING_ASC) 정렬")
        void loadReviewsByAuditorium_RATING_ASC() throws IOException {
            // given
            sut.createReview(getReviewRequest(seat1, 1, "r2"), user1.getId());
            sut.createReview(getReviewRequest(seat1, 5, "r5"), user1.getId());
            sut.createReview(getReviewRequest(seat1, 3, "r3"), user1.getId());
            var pageRequest = PageRequest.builder().page(1).size(10).build();

            // when
            SliceResponse<ReviewListResponse> response = sut.loadReviewsByAuditoriumId(auditorium.getId(), pageRequest, ReviewSortType.RATING_ASC);

            // then
            List<ReviewListResponse> result = response.content();
            Assertions.assertThat(result.get(0).content()).isEqualTo("r2");
            Assertions.assertThat(result.get(1).content()).isEqualTo("r3");
            Assertions.assertThat(result.get(2).content()).isEqualTo("r5");
        }

        @Test
        @DisplayName("[happy] 좋아요 내림차순(LIKES) 정렬")
        void loadReviewsByAuditorium_LIKES() throws IOException {
            // given
            Review review = sut.createReview(getReviewRequest(seat1, 2, "AAA"), user1.getId());
            Review review1 = sut.createReview(getReviewRequest(seat1, 2, "BBB"), user1.getId());
            Review review2 = sut.createReview(getReviewRequest(seat1, 2, "CCC"), user1.getId());

            likeService.reviewLike(user1.getId(), review2.getId());
            likeService.reviewLike(user2.getId(), review2.getId());
            likeService.reviewLike(user1.getId(), review1.getId());

            var pageRequest = PageRequest.builder().page(1).size(10).build();

            // when
            SliceResponse<ReviewListResponse> response = sut.loadReviewsByAuditoriumId(auditorium.getId(), pageRequest, ReviewSortType.LIKES);

            // then
            List<ReviewListResponse> result = response.content();
            Assertions.assertThat(result.get(0).content()).isEqualTo("CCC");
            Assertions.assertThat(result.get(0).heartCount()).isEqualTo(2L);
            Assertions.assertThat(result.get(1).content()).isEqualTo("BBB");
            Assertions.assertThat(result.get(1).heartCount()).isEqualTo(1L);
            Assertions.assertThat(result.get(2).content()).isEqualTo("AAA");
            Assertions.assertThat(result.get(2).heartCount()).isEqualTo(0L);
        }
    }





    /// 요청DTO 생성
    private ReviewRequest getReviewRequest(Seat seat, double rating) {
        return ReviewRequest.builder()
                .seatIds(List.of(seat.getId()))
                .title("review title")
                .content("test1")
                .movieTitle("ReviewTestTitle1")
                .imageUrls(null)
                .rating(rating)
                .hashtags(List.of(hashTag1.getId(), hashTag2.getId(), hashTag3.getId()))
                .build();
    }

    private ReviewRequest getReviewRequest(List<Seat> seats, double rating) {

        /// 좌석 번호
        List<String> seatIds = seats.stream()
                .map(Seat::getId)
                .toList();

        return ReviewRequest.builder()
                .seatIds(seatIds)
                .title("review title")
                .content("test1")
                .movieTitle("ReviewTestTitle1")
                .imageUrls(null)
                .rating(rating)
                .hashtags(List.of(hashTag1.getId(), hashTag2.getId(), hashTag3.getId()))
                .build();
    }

    private ReviewRequest getReviewRequest(List<Seat> seats, double rating, String content) {

        /// 좌석 번호
        List<String> seatIds = seats.stream()
                .map(Seat::getId)
                .toList();

        return ReviewRequest.builder()
                .seatIds(seatIds)
                .title("review title")
                .content(content)
                .movieTitle("ReviewTestTitle1")
                .imageUrls(null)
                .rating(rating)
                .hashtags(List.of(hashTag1.getId(), hashTag2.getId(), hashTag3.getId()))
                .build();
    }

    private ReviewRequest getReviewRequest(Seat seat, double rating, String content) {
        return ReviewRequest.builder()
                .seatIds(List.of(seat.getId()))
                .title("test")
                .content(content)
                .movieTitle("ReviewTestTitle1")
                .imageUrls(null)
                .rating(rating)
                .hashtags(List.of(hashTag1.getId(), hashTag2.getId(), hashTag3.getId()))
                .build();
    }

}
