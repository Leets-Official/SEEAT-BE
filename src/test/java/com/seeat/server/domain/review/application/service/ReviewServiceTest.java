package com.seeat.server.domain.review.application.service;

import com.seeat.server.domain.review.domain.ReviewFixtures;
import com.seeat.server.domain.review.domain.entity.Review;
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
import com.seeat.server.global.response.pageable.PageResponse;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.NoSuchElementException;

/**
 * [리뷰 서비스의 통합 테스트 클래스]입니다.
 * 리뷰 생성, 조회에 대한 happy/unhappy 테스트를 수행합니다.
 */
@SpringBootTest
@ActiveProfiles("test")
@Transactional
class ReviewServiceTest {

    @Autowired
    private ReviewService sut;

    @Autowired
    private ReviewRepository repository;

    @Autowired
    private SeatRepository seatRepository;

    @Autowired
    private AuditoriumRepository auditoriumRepository;

    @Autowired
    private TheaterRepository theaterRepository;

    @Autowired
    private UserRepository userRepository;

    private Theater theater;
    private Auditorium auditorium;
    private Seat seat;
    private User user;

    /**
     * 각 테스트 실행 전 공통으로 필요한 영화관, 상영관, 좌석, 유저 데이터를 준비합니다.
     */
    @BeforeEach
    void setUp() {
        theater = theaterRepository.save(TheaterFixtures.createTheater());
        auditorium = auditoriumRepository.save(AuditoriumFixtures.createAuditorium(theater));
        seat = seatRepository.save(SeatFixtures.createSeat(auditorium));
        user = userRepository.save(UserFixtures.createUser());
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
                    .seatId(seat.getId())
                    .content("test")
                    .movieTitle("ReviewTestTitle")
                    .photos(null)
                    .rating(5)
                    .hashtags(List.of(1L, 2L))
                    .build();

            //when
            sut.createReview(request, user.getId());

            //then
            List<Review> reviews = repository.findAll();
            Assertions.assertThat(reviews).hasSize(1);
            Review review = reviews.get(0);
            Assertions.assertThat(review.getContent()).isEqualTo(request.content());
            Assertions.assertThat(review.getRating()).isEqualTo(request.rating());
            Assertions.assertThat(review.getSeat().getId()).isEqualTo(seat.getId());
            Assertions.assertThat(review.getSeat().getAuditorium().getTheater().getName()).isEqualTo("Test Theater");
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
                    .seatId(seat.getId())
                    .content("test")
                    .movieTitle("ReviewTestTitle")
                    .photos(null)
                    .rating(5)
                    .hashtags(List.of(1L, 2L))
                    .build();

            // when & then
            Assertions.assertThatThrownBy(() -> sut.createReview(request, fakeUser.getId()))
                    .isInstanceOf(NoSuchElementException.class)
                    .hasMessageContaining(ErrorCode.NOT_USER.getMessage());
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
            Review review = repository.save(ReviewFixtures.createReview(user, seat));

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
            Review fakeReview = ReviewFixtures.fakeReview(user, seat);

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

            Review review1 = repository.save(ReviewFixtures.createReview(user, seat, 5, firstReview));
            Review review2 = repository.save(ReviewFixtures.createReview(user, seat, 3, secondReview));
            var pageRequest = PageRequest.builder().page(0).size(10).build();

            //when
            PageResponse<ReviewListResponse> response = sut.loadReviewsBySeatId(seat.getId(), pageRequest);

            //then
            List<ReviewListResponse> responses = response.getDtoList();
            Assertions.assertThat(responses).hasSize(2);
            ReviewListResponse response1 = responses.get(0);
            ReviewListResponse response2 = responses.get(1);

            Assertions.assertThat(response1.content()).isEqualTo(review1.getContent());
            Assertions.assertThat(response2.content()).isEqualTo(review2.getContent());
        }


        @Test
        @DisplayName("[happy] 영화관 기반_리뷰 목록 조회")
        void loadReviews_happy_theater() {
            // given
            String firstReview = "첫번째 리뷰입니다.";
            String secondReview = "두번째 리뷰입니다.";
            String thirdReview = "세번째 리뷰입니다.";
            String fourthReview = "네번째 리뷰입니다.";

            Review review1 = repository.save(ReviewFixtures.createReview(user, seat, 1, firstReview));
            Review review2 = repository.save(ReviewFixtures.createReview(user, seat, 2, secondReview));
            Review review3 = repository.save(ReviewFixtures.createReview(user, seat, 3, thirdReview));
            Review review4 = repository.save(ReviewFixtures.createReview(user, seat, 4, fourthReview));
            var pageRequest = PageRequest.builder().page(0).size(10).build();

            // when
            PageResponse<ReviewListResponse> response = sut.loadReviewsByTheaterId(theater.getId(), pageRequest);

            // then
            List<ReviewListResponse> responses = response.getDtoList();
            Assertions.assertThat(responses).hasSize(4);

            // 각 리뷰의 내용이 저장한 순서대로 반환되는지 검증
            Assertions.assertThat(responses.get(0).content()).isEqualTo(firstReview);
            Assertions.assertThat(responses.get(1).content()).isEqualTo(secondReview);
            Assertions.assertThat(responses.get(2).content()).isEqualTo(thirdReview);
            Assertions.assertThat(responses.get(3).content()).isEqualTo(fourthReview);
        }
    }
}
