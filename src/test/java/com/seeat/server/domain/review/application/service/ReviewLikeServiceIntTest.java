package com.seeat.server.domain.review.application.service;

import com.seeat.server.domain.review.domain.ReviewFixtures;
import com.seeat.server.domain.review.domain.ReviewLikeFixtures;
import com.seeat.server.domain.review.domain.entity.Review;
import com.seeat.server.domain.review.domain.entity.ReviewLike;
import com.seeat.server.domain.review.domain.repository.ReviewLikeRepository;
import com.seeat.server.domain.review.domain.repository.ReviewRepository;
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
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;

@SpringBootTest
@Transactional
@ActiveProfiles("test")
class ReviewLikeServiceIntTest {

    @Autowired
    private ReviewLikeService sut;

    @Autowired
    private ReviewLikeRepository repository;

    /// 기타 의존성
    @Autowired
    private UserRepository userRepository;

    @Autowired
    private AuditoriumRepository auditoriumRepository;

    @Autowired
    private TheaterRepository theaterRepository;

    @Autowired
    private SeatRepository seatRepository;

    @Autowired
    private ReviewRepository reviewRepository;

    private Seat seat;
    private User user;
    private Theater theater;
    private Auditorium auditorium;
    private Review review;

    @BeforeEach()
    void setUp() {
        theater = theaterRepository.save(TheaterFixtures.createTheater());
        auditorium = auditoriumRepository.save(AuditoriumFixtures.createAuditorium(theater));
        seat = seatRepository.save(SeatFixtures.createSeat(auditorium));
        user = userRepository.save(UserFixtures.createUser());
        review = reviewRepository.save(ReviewFixtures.createReview(user, seat));
    }

    @Nested
    @DisplayName("생성 테스트")
    class createReviewLikeTests {

        @Test
        @DisplayName("[happy] 정상적으로 좋아요 생성")
        public void createTest_happy(){

            //given

            //when
            sut.reviewLike(user.getId(), review.getId());

            //then
            boolean checked = repository.existsByUserAndReview(user, review);
            Assertions.assertTrue(checked);
        }

        @Test
        @DisplayName("[unhappy] 이미 좋아요를 누른 리뷰에 다시 좋아요를 누를 경우")
        public void createTest_unhappy_throw_Duplicate(){

            //given
            sut.reviewLike(user.getId(), review.getId());

            //when & then
            assertThatThrownBy(() -> sut.reviewLike(user.getId(), review.getId()))
                    .isInstanceOf(IllegalStateException.class)
                    .hasMessage(ErrorCode.DUPLICATE_REVIEW.getMessage());
        }


    }


    @Nested
    @DisplayName("삭제 테스트")
    class deleteReviewLikeTests {

        @Test
        @DisplayName("[happy] 정상적으로 좋아요 삭제")
        public void createTest_happy(){

            //given
            ReviewLike stub = ReviewLikeFixtures.stub(user, review);
            repository.save(stub);

            //when
            sut.reviewCancel(user.getId(), review.getId());

            //then
            boolean checked = repository.existsByUserAndReview(user, review);
            Assertions.assertFalse(checked);
        }

        @Test
        @DisplayName("[unhappy] 좋아요를 누르지 않은 리뷰에 해지할 경우")
        public void createTest_unhappy_throw_not_own(){

            //given

            //when & then
            assertThatThrownBy(() -> sut.reviewCancel(user.getId(), review.getId()))
                    .isInstanceOf(IllegalStateException.class)
                    .hasMessage(ErrorCode.NOT_OWN_REVIEW.getMessage());
        }

    }




}
