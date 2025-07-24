package com.seeat.server.domain.review.application.service;

import com.seeat.server.domain.best.application.dto.response.BestReviewListResponse;
import com.seeat.server.domain.review.application.usecase.ReviewBestContentMediatorUseCase;
import com.seeat.server.domain.review.application.usecase.ReviewLikeUseCase;
import com.seeat.server.domain.review.domain.ReviewFixtures;
import com.seeat.server.domain.review.domain.entity.Review;
import com.seeat.server.domain.review.domain.repository.HashTagRepository;
import com.seeat.server.domain.review.domain.repository.ReviewRepository;
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
import com.seeat.server.global.response.pageable.PageRequest;
import com.seeat.server.global.response.pageable.SliceResponse;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.util.List;

@SpringBootTest
@Transactional
@ActiveProfiles("test")
class ReviewBestContentMediatorIntTest {

    @Autowired
    private ReviewBestContentMediatorUseCase sut;

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


    /// 좋아요 의존성 추가
    @Autowired
    private ReviewLikeUseCase likeService;

    private Seat seat1;
    private Seat seat2;
    private User user1;
    private Theater theater;
    private Auditorium auditorium;

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
    }

    @Nested
    @DisplayName("인기 목록 조회 테스트")
    class LoadReviewsPopular {

        @Test
        @DisplayName("[happy] 인기순정렬_기본케이스")
        public void loadPopular() {

            //given
            PageRequest pageRequest = PageRequest.builder().page(1).size(8).build();
            Review review1 = repository.save(ReviewFixtures.createReview(user1, seat1));
            Review review2 = repository.save(ReviewFixtures.createReview(user1, seat2));

            // 좋아요 추가
            likeService.reviewLike(user1.getId(), review1.getId());

            //when
            SliceResponse<BestReviewListResponse> response = sut.getBestReviews(pageRequest);

            // then
            List<BestReviewListResponse> contents = response.content();

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
            Review older = repository.save(ReviewFixtures.createReview(user1, seat1)); // 먼저 저장, 좋아요 1개
            Review newer = repository.save(ReviewFixtures.createReview(user1, seat2)); // 나중 저장, 좋아요 1개
            likeService.reviewLike(user1.getId(), older.getId());
            likeService.reviewLike(user1.getId(), newer.getId());

            // when
            SliceResponse<BestReviewListResponse> response = sut.getBestReviews(pageRequest);
            List<BestReviewListResponse> contents = response.content();

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
            Review revA = repository.save(ReviewFixtures.createReview(user1, seat1));
            Review revB = repository.save(ReviewFixtures.createReview(user1, seat2));
            User user2 = userRepository.save(UserFixtures.createUser());
            User user3 = userRepository.save(UserFixtures.createUser());

            // A: 3명, B: 2명(중복 허용X)
            likeService.reviewLike(user1.getId(), revA.getId());
            likeService.reviewLike(user2.getId(), revA.getId());
            likeService.reviewLike(user3.getId(), revA.getId());
            likeService.reviewLike(user1.getId(), revB.getId());
            likeService.reviewLike(user2.getId(), revB.getId());

            // when
            SliceResponse<BestReviewListResponse> response = sut.getBestReviews(pageRequest);
            List<BestReviewListResponse> contents = response.content();

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
            SliceResponse<BestReviewListResponse> response = sut.getBestReviews(pageRequest);
            List<BestReviewListResponse> contents = response.content();

            // then
            Assertions.assertThat(contents).isEmpty();
        }

    }

}
