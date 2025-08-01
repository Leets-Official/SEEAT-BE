package com.seeat.server.domain.review.application.service;

import com.seeat.server.domain.image.domain.entity.ReviewImage;
import com.seeat.server.domain.review.application.dto.request.ReviewUpdateRequest;
import com.seeat.server.domain.review.application.usecase.ReviewLikeUseCase;
import com.seeat.server.domain.review.domain.HashTagFixtures;
import com.seeat.server.domain.review.domain.ReviewFixtures;
import com.seeat.server.domain.review.domain.entity.*;
import com.seeat.server.domain.review.domain.repository.HashTagRepository;
import com.seeat.server.domain.review.domain.repository.ReviewHashTagRepository;
import com.seeat.server.domain.image.domain.repository.ReviewImageRepository;
import com.seeat.server.domain.review.domain.repository.ReviewRepository;
import com.seeat.server.domain.review.application.dto.request.ReviewRequest;
import com.seeat.server.domain.review.application.dto.response.ReviewDetailResponse;
import com.seeat.server.domain.review.application.dto.response.ReviewListResponse;
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
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.NoSuchElementException;

import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;
import static org.assertj.core.api.AssertionsForInterfaceTypes.assertThat;
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
            var request = ReviewRequest.builder()
                    .seatIds(List.of(seat1.getId()))
                    .content("test")
                    .movieTitle("ReviewTestTitle")
                    .imageUrls(null)
                    .rating(5)
                    .hashtags(List.of(hashTag1.getId(), hashTag2.getId(), hashTag3.getId()))
                    .build();

            //when
            sut.createReview(request, user1.getId());

            //then
            /// 리뷰 체크
            List<Review> reviews = repository.findAll();

            // 1. 개수 검증
            Assertions.assertThat(reviews).hasSize(1);

            // 2. 리뷰와 정보들이 일치하는지 검증
            Review review = reviews.get(0);
            Assertions.assertThat(review.getContent()).isEqualTo(request.getContent());
            Assertions.assertThat(review.getRating()).isEqualTo(request.getRating());
            Assertions.assertThat(review.getSeat().getId()).isEqualTo(seat1.getId());
            Assertions.assertThat(review.getSeat().getAuditorium().getTheater().getName()).isEqualTo("Test Theater");

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
            var request = ReviewRequest.builder()
                    .seatIds(List.of(seat1.getId(), seat2.getId()))
                    .content("test")
                    .movieTitle("ReviewTestTitle")
                    .imageUrls(null)
                    .rating(5)
                    .hashtags(List.of(hashTag1.getId(), hashTag2.getId(), hashTag3.getId()))
                    .build();

            // when
            sut.createReview(request, user1.getId());

            // then
            List<Review> reviews = repository.findAll();
            Assertions.assertThat(reviews).hasSize(2);

            for (Review review : reviews) {
                Assertions.assertThat(review.getContent()).isEqualTo(request.getContent());
                Assertions.assertThat(review.getRating()).isEqualTo(request.getRating());
                Assertions.assertThat(List.of(seat1.getId(), seat2.getId())).contains(review.getSeat().getId());
                Assertions.assertThat(review.getSeat().getAuditorium().getTheater().getName())
                        .isEqualTo("Test Theater");

                List<ReviewHashTag> reviewHashTags = reviewHashTagRepository.findByReview(review);
                Assertions.assertThat(reviewHashTags).hasSize(3);

                List<Long> actualHashTagIds = reviewHashTags.stream()
                        .map(rht -> rht.getHashTag().getId())
                        .toList();

                Assertions.assertThat(actualHashTagIds)
                        .containsExactlyInAnyOrderElementsOf(request.getHashtags());
            }
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
                    .content("test")
                    .movieTitle("ReviewTestTitle")
                    .rating(5)
                    .hashtags(List.of(hashTag1.getId(), hashTag2.getId(), hashTag3.getId()))
                    .imageUrls(List.of("file1.jpg"))
                    .build();

            //when
            sut.createReview(request, user1.getId());

            //then
            /// 리뷰 체크
            List<Review> reviews = repository.findAll();

            // 1. 개수 검증
            Assertions.assertThat(reviews).hasSize(1);

            // 2. 리뷰와 정보들이 일치하는지 검증
            Review review = reviews.get(0);
            Assertions.assertThat(review.getContent()).isEqualTo(request.getContent());
            Assertions.assertThat(review.getRating()).isEqualTo(request.getRating());
            Assertions.assertThat(review.getSeat().getId()).isEqualTo(seat1.getId());
            Assertions.assertThat(review.getSeat().getAuditorium().getTheater().getName()).isEqualTo("Test Theater");

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

            // 이미지 검증
            String thumbnailUrl = review.getThumbnailUrl();
            Assertions.assertThat(thumbnailUrl.contains("file1.jpg"));
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
                    .content("test")
                    .movieTitle("ReviewTestTitle")
                    .rating(3)
                    .hashtags(List.of(hashTag1.getId(), hashTag2.getId(), hashTag3.getId()))
                    .imageUrls(List.of("file1.jpg", "file2.png", "file3.jpeg"))
                    .build();

            //when
            sut.createReview(request, user1.getId());

            //then
            /// 리뷰 체크
            List<Review> reviews = repository.findAll();

            // 1. 개수 검증
            Assertions.assertThat(reviews).hasSize(1);

            // 2. 리뷰와 정보들이 일치하는지 검증
            Review review = reviews.get(0);
            Assertions.assertThat(review.getContent()).isEqualTo(request.getContent());
            Assertions.assertThat(review.getRating()).isEqualTo(request.getRating());
            Assertions.assertThat(review.getSeat().getId()).isEqualTo(seat1.getId());
            Assertions.assertThat(review.getSeat().getAuditorium().getTheater().getName()).isEqualTo("Test Theater");

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

            //3. 미지 검증
            List<ReviewImage> reviewImages = imageRepository.findByReview(review);
            Assertions.assertThat(reviewImages).hasSize(3);

            // 파일명 포함 여부 확인
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
        void loadReview_happy() {
            //given
            Review review = repository.save(ReviewFixtures.createReview(user1, seat1));

            //when
            ReviewDetailResponse response = sut.loadReview(review.getId());

            //then
            Assertions.assertThat(response).isNotNull();
            Assertions.assertThat(response.content()).isEqualTo(review.getContent());
            Assertions.assertThat(response.rating()).isEqualTo(review.getRating());
            Assertions.assertThat(response.movieSeatInfo().movieTitle()).isEqualTo(review.getMovieTitle());
            Assertions.assertThat(response.movieSeatInfo().theaterName()).isEqualTo(review.getSeat().getAuditorium().getTheater().getName());
        }

        /**
         * 존재하지 않는 리뷰 조회 시 예외가 발생하는지 검증합니다.
         */
        @Test
        @DisplayName("[unhappy] 존재하지 않는 리뷰 조회")
        void loadReview_unhappy_not_id() {
            //given
            Review fakeReview = ReviewFixtures.fakeReview(user1, seat1);

            // when & then
            Assertions.assertThatThrownBy(() -> sut.loadReview(fakeReview.getId()))
                    .isInstanceOf(NoSuchElementException.class)
                    .hasMessageContaining(ErrorCode.NOT_REVIEW.getMessage());
        }

        /**
         * 리뷰 목록 조회가 정상 동작하는지 검증합니다.
         */
        @Test
        @DisplayName("[happy] 좌석 기반_리뷰 목록 조회")
        void loadReviews_happy_seat() {
            //given
            String firstReview = "첫번째 리뷰입니다.";
            String secondReview = "두번째 리뷰입니다.";

            Review review1 = repository.save(ReviewFixtures.createReview(user1, seat1, 5, firstReview));
            Review review2 = repository.save(ReviewFixtures.createReview(user1, seat1, 3, secondReview));
            var pageRequest = PageRequest.builder().page(1).size(10).build();

            //when
            SliceResponse<ReviewListResponse> response = sut.loadReviewsBySeatId(seat1.getId(), pageRequest);

            //then
            List<ReviewListResponse> responses = response.content();
            Assertions.assertThat(responses).hasSize(2);
            ReviewListResponse response1 = responses.get(0);
            ReviewListResponse response2 = responses.get(1);

            Assertions.assertThat(response1.content()).isEqualTo(review2.getContent());
            Assertions.assertThat(response2.content()).isEqualTo(review1.getContent());
        }


        @Test
        @DisplayName("[happy] 상영관 기반_리뷰 목록 조회")
        void loadReviews_happy_theater() {
            // given
            String firstReview = "첫번째 리뷰입니다.";
            String secondReview = "두번째 리뷰입니다.";
            String thirdReview = "세번째 리뷰입니다.";
            String fourthReview = "네번째 리뷰입니다.";

            Review review1 = repository.save(ReviewFixtures.createReview(user1, seat1, 1, firstReview));
            Review review2 = repository.save(ReviewFixtures.createReview(user1, seat1, 2, secondReview));
            Review review3 = repository.save(ReviewFixtures.createReview(user1, seat1, 3, thirdReview));
            Review review4 = repository.save(ReviewFixtures.createReview(user1, seat1, 4, fourthReview));
            var pageRequest = PageRequest.builder().page(1).size(10).build();

            // when
            SliceResponse<ReviewListResponse> response = sut.loadReviewsByAuditoriumId(auditorium.getId(), pageRequest);

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
            var request = ReviewRequest.builder()
                    .seatIds(List.of(seat1.getId()))
                    .content("test")
                    .movieTitle("ReviewTestTitle")
                    .imageUrls(null)
                    .rating(5)
                    .hashtags(List.of(hashTag1.getId(), hashTag2.getId(), hashTag3.getId()))
                    .build();

            //when
            List<Review> sutReview = sut.createReview(request, user1.getId());

            likeService.reviewLike(user1.getId(), sutReview.get(0).getId());
            likeService.reviewLike(user2.getId(), sutReview.get(0).getId());

            //when
            ReviewDetailResponse response = sut.loadReview(sutReview.get(0).getId());

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
                    .content("test1")
                    .movieTitle("ReviewTestTitle1")
                    .imageUrls(null)
                    .rating(5)
                    .hashtags(List.of(hashTag1.getId(), hashTag2.getId(), hashTag3.getId()))
                    .build();

            var request2 = ReviewRequest.builder()
                    .seatIds(List.of(seat1.getId()))
                    .content("test2")
                    .movieTitle("ReviewTestTitle2")
                    .imageUrls(null)
                    .rating(3)
                    .hashtags(List.of(hashTag1.getId(), hashTag2.getId(), hashTag3.getId()))
                    .build();

            List<Review> sutReview1 = sut.createReview(request1, user1.getId());
            List<Review> sutReview2 = sut.createReview(request2, user1.getId());

            // 1에게만 좋아요 누르기
            likeService.reviewLike(user1.getId(), sutReview1.get(0).getId());
            likeService.reviewLike(user2.getId(), sutReview1.get(0).getId());

            // when
            SliceResponse<ReviewListResponse> response = sut.loadReviewsByAuditoriumId(auditorium.getId(), pageRequest);

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
            List<Review> reviews = sut.createReview(request, user1.getId());
            Review review = reviews.get(0);

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
            Assertions.assertThat(savedReview.getSeat()).isEqualTo(seat1);

        }

        @Test
        @DisplayName("[happy] 사진 포함, 리뷰의 작성자는 정상적으로 수정할 수 있습니다.")
        void update_happy_imageUrls() throws IOException {

            // given
            /// 요청
            var request = getReviewRequest(seat1, 4);

            /// 생성
            List<Review> reviews = sut.createReview(request, user1.getId());
            Review review = reviews.get(0);

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
            Assertions.assertThat(savedReview.getSeat()).isEqualTo(seat1);

        }

        @Test
        @DisplayName("[unhappy] 존재하지 않는 사용자는 수정할 수 없습니다.")
        void update_throws_not_users() throws IOException {

            // given
            Long attackedUserId = 99999999L;

            var request = getReviewRequest(seat1, 4);

            /// 생성
            List<Review> reviews = sut.createReview(request, user1.getId());
            Review review = reviews.get(0);

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
            List<Review> reviews = sut.createReview(request, user1.getId());
            Review review = reviews.get(0);

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

            /// 요청
            var request = getReviewRequest(seat1, 4);

            /// 생성
            List<Review> reviews = sut.createReview(request, user1.getId());
            Review review = reviews.get(0);

            // when
            sut.deleteReview(review.getId(), user1.getId());

            // then
            /// DB에 존재하는지 체크
            assertTrue(repository.findById(review.getId()).isEmpty());

            /// 해시태그 존재하는지 체크
            List<ReviewHashTag> hashTags = reviewHashTagRepository.findByReview_Id(review.getId());
            Assertions.assertThat(hashTags).isEmpty();

            /// 이미지 존재하는지 체크
            List<ReviewImage> images = imageRepository.findByReview(review);
            Assertions.assertThat(images).isEmpty();
        }

        @Test
        @DisplayName("[unhappy] 존재하지 않는 사용자는 삭제할 수 없습니다.")
        void delete_throws_not_users() throws IOException {

            // given
            Long attackedUserId = 99999999L;

            var request = getReviewRequest(seat1, 4);

            /// 생성
            List<Review> reviews = sut.createReview(request, user1.getId());
            Review review = reviews.get(0);

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
            List<Review> reviews = sut.createReview(request, user1.getId());
            Review review = reviews.get(0);

            // when & then
            assertThatThrownBy(() -> sut.deleteReview(review.getId(), attackedUserId))
                    .isInstanceOf(NoSuchElementException.class)
                    .hasMessage(ErrorCode.NOT_OWN_USER_REVIEW.getMessage());

        }
    }


    private ReviewRequest getReviewRequest(Seat seat, double rating) {
        return ReviewRequest.builder()
                .seatIds(List.of(seat.getId()))
                .content("test1")
                .movieTitle("ReviewTestTitle1")
                .imageUrls(null)
                .rating(rating)
                .hashtags(List.of(hashTag1.getId(), hashTag2.getId(), hashTag3.getId()))
                .build();
    }

}
