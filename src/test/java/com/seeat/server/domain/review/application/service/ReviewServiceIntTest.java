package com.seeat.server.domain.review.application.service;

import com.seeat.server.domain.review.application.usecase.ReviewLikeUseCase;
import com.seeat.server.domain.review.domain.HashTagFixtures;
import com.seeat.server.domain.review.domain.ReviewFixtures;
import com.seeat.server.domain.review.domain.entity.HashTag;
import com.seeat.server.domain.review.domain.entity.HashTagType;
import com.seeat.server.domain.review.domain.entity.Review;
import com.seeat.server.domain.review.domain.entity.ReviewHashTag;
import com.seeat.server.domain.review.domain.repository.HashTagRepository;
import com.seeat.server.domain.review.domain.repository.ReviewHashTagRepository;
import com.seeat.server.domain.review.domain.repository.ReviewRepository;
import com.seeat.server.domain.review.application.dto.request.ReviewRequest;
import com.seeat.server.domain.review.application.dto.response.ReviewDetailResponse;
import com.seeat.server.domain.review.application.dto.response.ReviewListResponse;
import com.seeat.server.domain.seat.domain.AuditoriumFixtures;
import com.seeat.server.domain.seat.domain.SeatFixtures;
import com.seeat.server.domain.seat.domain.TheaterFixtures;
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

import java.util.List;
import java.util.NoSuchElementException;

import static org.junit.Assert.assertFalse;

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

    private Seat seat1;
    private Seat seat2;
    private User user;


    private Theater theater;
    private Auditorium auditorium;
    private HashTag hashTag1;
    private HashTag hashTag2;
    private HashTag hashTag3;

    /**
     * 각 테스트 실행 전 공통으로 필요한 영화관, 상영관, 좌석, 유저 데이터를 준비합니다.
     */
    @BeforeEach
    void setUp() {
        theater = theaterRepository.save(TheaterFixtures.createTheater());
        auditorium = auditoriumRepository.save(AuditoriumFixtures.createAuditorium(theater));
        seat1 = seatRepository.save(SeatFixtures.createSeat(auditorium));
        seat2 = seatRepository.save(SeatFixtures.createSeat2(auditorium));
        user = userRepository.save(UserFixtures.createUser());
        hashTag1 = hashTagRepository.save(HashTagFixtures.createHashTag(HashTagType.SOUND, "음향이 좋아요"));
        hashTag2 = hashTagRepository.save(HashTagFixtures.createHashTag(HashTagType.COMPANION, "혼자 관람했어요"));
        hashTag3 = hashTagRepository.save(HashTagFixtures.createHashTag(HashTagType.ENVIRONMENT, "좌석이 넓어요"));
    }

    @Nested
    @DisplayName("생성 테스트")
    class CreateReview {

        /**
         * 정상적인 유저가 리뷰를 생성하는 경우를 검증합니다.
         */
        @Test
        @DisplayName("[happy] 로그인한 유저 리뷰 정상 생성")
        void createReviewByUser_happy() {
            //given
            var request = ReviewRequest.builder()
                    .seatId(seat1.getId())
                    .content("test")
                    .movieTitle("ReviewTestTitle")
                    .photos(null)
                    .rating(5)
                    .hashtags(List.of(hashTag1.getId(), hashTag2.getId(), hashTag3.getId()))
                    .build();

            //when
            sut.createReview(request, user.getId());

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

        /**
         * 존재하지 않는 유저가 리뷰를 생성할 때 예외가 발생하는지 검증합니다.
         */
        @Test
        @DisplayName("[unhappy] DB에 존재하지 않는 유저가 리뷰 생성시 예외 발생")
        void createReviewByUser_unhappy_throw_NoSuchElementException() {
            //given
            User fakeUser = UserFixtures.fakeUser();
            var request = ReviewRequest.builder()
                    .seatId(seat1.getId())
                    .content("test")
                    .movieTitle("ReviewTestTitle")
                    .photos(null)
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
                    .seatId(seat1.getId())
                    .content("test")
                    .movieTitle("ReviewTestTitle")
                    .photos(null)
                    .rating(5)
                    .hashtags(List.of(hashTag1.getId(), hashTag2.getId()))
                    .build();

            // when & then
            Assertions.assertThatThrownBy(() -> sut.createReview(request, user.getId()))
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
            Review review = repository.save(ReviewFixtures.createReview(user, seat1));

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
            Review fakeReview = ReviewFixtures.fakeReview(user, seat1);

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

            Review review1 = repository.save(ReviewFixtures.createReview(user, seat1, 5, firstReview));
            Review review2 = repository.save(ReviewFixtures.createReview(user, seat1, 3, secondReview));
            var pageRequest = PageRequest.builder().page(1).size(10).build();

            //when
            SliceResponse<ReviewListResponse> response = sut.loadReviewsBySeatId(seat1.getId(), pageRequest);

            //then
            List<ReviewListResponse> responses = response.content();
            Assertions.assertThat(responses).hasSize(2);
            ReviewListResponse response1 = responses.get(0);
            ReviewListResponse response2 = responses.get(1);

            Assertions.assertThat(response1.content()).isEqualTo(review1.getContent());
            Assertions.assertThat(response2.content()).isEqualTo(review2.getContent());
        }


        @Test
        @DisplayName("[happy] 상영관 기반_리뷰 목록 조회")
        void loadReviews_happy_theater() {
            // given
            String firstReview = "첫번째 리뷰입니다.";
            String secondReview = "두번째 리뷰입니다.";
            String thirdReview = "세번째 리뷰입니다.";
            String fourthReview = "네번째 리뷰입니다.";

            Review review1 = repository.save(ReviewFixtures.createReview(user, seat1, 1, firstReview));
            Review review2 = repository.save(ReviewFixtures.createReview(user, seat1, 2, secondReview));
            Review review3 = repository.save(ReviewFixtures.createReview(user, seat1, 3, thirdReview));
            Review review4 = repository.save(ReviewFixtures.createReview(user, seat1, 4, fourthReview));
            var pageRequest = PageRequest.builder().page(1).size(10).build();

            // when
            SliceResponse<ReviewListResponse> response = sut.loadReviewsByAuditoriumId(auditorium.getId(), pageRequest);

            // then
            List<ReviewListResponse> responses = response.content();
            Assertions.assertThat(responses).hasSize(4);

            // 각 리뷰의 내용이 저장한 순서대로 반환되는지 검증
            Assertions.assertThat(responses.get(0).content()).isEqualTo(firstReview);
            Assertions.assertThat(responses.get(1).content()).isEqualTo(secondReview);
            Assertions.assertThat(responses.get(2).content()).isEqualTo(thirdReview);
            Assertions.assertThat(responses.get(3).content()).isEqualTo(fourthReview);
        }
    }

    @Nested
    @DisplayName("인기 목록 조회 테스트")
    class LoadReviewsPopular {

        @Test
        @DisplayName("[happy] 인기순정렬_기본케이스")
        public void loadPopular() {

            //given
            PageRequest pageRequest = PageRequest.builder().page(1).size(8).build();
            Review review1 = repository.save(ReviewFixtures.createReview(user, seat1));
            Review review2 = repository.save(ReviewFixtures.createReview(user, seat2));

            // 좋아요 추가
            likeService.reviewLike(user.getId(), review1.getId());

            //when
            SliceResponse<ReviewListResponse> response = sut.loadFavoriteReviews(pageRequest);

            // then
            List<ReviewListResponse> contents = response.content();

            // 응답 리스트가 Null 이 아니고 사이즈가 2개인지 확인
            Assertions.assertThat(contents).isNotNull();
            Assertions.assertThat(contents.size()).isEqualTo(2);

            // 순서 검증: 좋아요 2개 받은 review2가 먼저, 그 다음 review1
            Assertions.assertThat(contents.get(0).reviewId()).isEqualTo(review1.getId());
            Assertions.assertThat(contents.get(1).reviewId()).isEqualTo(review2.getId());
            org.junit.jupiter.api.Assertions.assertFalse(response.hasNext());
        }

        @Test
        @DisplayName("[happy] 좋아요 수가 같은 경우 최신순 정렬")
        void loadPopular_same_new() {
            // given
            PageRequest pageRequest = PageRequest.builder().page(1).size(10).build();
            Review older = repository.save(ReviewFixtures.createReview(user, seat1)); // 먼저 저장, 좋아요 1개
            Review newer = repository.save(ReviewFixtures.createReview(user, seat2)); // 나중 저장, 좋아요 1개
            likeService.reviewLike(user.getId(), older.getId());
            likeService.reviewLike(user.getId(), newer.getId());

            // when
            SliceResponse<ReviewListResponse> response = sut.loadFavoriteReviews(pageRequest);
            List<ReviewListResponse> contents = response.content();

            // then
            Assertions.assertThat(contents.size()).isEqualTo(2);
            // (정책에 따라 최신 우선/생성순/ID 내림차순 등 원하는 로직 고정)
            Assertions.assertThat(contents.get(0).reviewId()).isEqualTo(newer.getId());
            Assertions.assertThat(contents.get(1).reviewId()).isEqualTo(older.getId());
        }

        @Test
        @DisplayName("[happy] 여러 유저가 복수 개 리뷰에 좋아요를 누른 경우 합산 집계 및 순위정렬의 정확성")
        void multiple_user_like() {
            // given
            PageRequest pageRequest = PageRequest.builder().page(1).size(10).build();
            Review revA = repository.save(ReviewFixtures.createReview(user, seat1));
            Review revB = repository.save(ReviewFixtures.createReview(user, seat2));
            User user2 = userRepository.save(UserFixtures.createUser());
            User user3 = userRepository.save(UserFixtures.createUser());

            // A: 3명, B: 2명(중복 허용X)
            likeService.reviewLike(user.getId(), revA.getId());
            likeService.reviewLike(user2.getId(), revA.getId());
            likeService.reviewLike(user3.getId(), revA.getId());
            likeService.reviewLike(user.getId(), revB.getId());
            likeService.reviewLike(user2.getId(), revB.getId());

            // when
            SliceResponse<ReviewListResponse> response = sut.loadFavoriteReviews(pageRequest);
            List<ReviewListResponse> contents = response.content();

            // then
            Assertions.assertThat(contents.size()).isEqualTo(2);
            Assertions.assertThat(contents.get(0).reviewId()).isEqualTo(revA.getId());
            Assertions.assertThat(contents.get(1).reviewId()).isEqualTo(revB.getId());
        }

        /**
         * 리뷰가 아예 없는 경우 빈 리스트임을 검증한다.
         */
        @Test
        @DisplayName("[happy] 등록된 리뷰가 없을 때 빈 리스트 반환")
        void loadPopular_empty() {
            // given
            PageRequest pageRequest = PageRequest.builder().page(1).size(5).build();

            // when
            SliceResponse<ReviewListResponse> response = sut.loadFavoriteReviews(pageRequest);
            List<ReviewListResponse> contents = response.content();

            // then
            Assertions.assertThat(contents).isEmpty();
        }


    }

}
