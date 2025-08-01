package com.seeat.server.domain.theater.application.service;

import com.seeat.server.domain.review.application.dto.request.ReviewRequest;
import com.seeat.server.domain.review.application.usecase.ReviewUseCase;
import com.seeat.server.domain.review.domain.HashTagFixtures;
import com.seeat.server.domain.review.domain.entity.HashTag;
import com.seeat.server.domain.review.domain.entity.HashTagType;
import com.seeat.server.domain.review.domain.repository.HashTagRepository;
import com.seeat.server.domain.theater.domain.AuditoriumFixtures;
import com.seeat.server.domain.theater.domain.SeatFixtures;
import com.seeat.server.domain.theater.domain.TheaterFixtures;
import com.seeat.server.domain.theater.application.dto.response.SeatRatingSummaryResponse;
import com.seeat.server.domain.theater.application.dto.response.SeatType;
import com.seeat.server.domain.theater.domain.entity.Auditorium;
import com.seeat.server.domain.theater.domain.entity.Seat;
import com.seeat.server.domain.theater.domain.entity.Theater;
import com.seeat.server.domain.theater.domain.repository.AuditoriumRepository;
import com.seeat.server.domain.theater.domain.repository.SeatRepository;
import com.seeat.server.domain.theater.domain.repository.TheaterRepository;
import com.seeat.server.domain.user.domain.UserFixtures;
import com.seeat.server.domain.user.domain.entity.User;
import com.seeat.server.domain.user.domain.repository.UserRepository;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
class SeatRatingServiceIntTest {

    @Autowired
    private SeatRatingService sut;

    /// 리뷰를 남기기 위한 의존성 주입
    @Autowired
    private ReviewUseCase reviewService;

    /// 시작하기전에 필요한 데이터 의존성
    @Autowired
    private UserRepository userRepository;

    @Autowired
    private HashTagRepository hashTagRepository;

    @Autowired
    private AuditoriumRepository auditoriumRepository;

    @Autowired
    private TheaterRepository theaterRepository;

    @Autowired
    private SeatRepository seatRepository;

    /// 값
    private Seat seat1;
    private Seat seat2;
    private User user1;
    private User user2;

    private Theater theater;
    private Auditorium auditorium;
    private HashTag hashTag1;
    private HashTag hashTag2;
    private HashTag hashTag3;

    @BeforeEach
    void setUp() {

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
    @DisplayName("[좌석 조회] 후기가 없는 좌석")
    class GetNoSeatRating {

        @Test
        @DisplayName("[happy] 후기가 없는 경우, 그냥 좌석배치도와 개수/평점이 0이 되도록 출력되도록 진행")
        public void noReview() throws Exception {

            //given

            //when
            List<SeatRatingSummaryResponse> summaries = sut.getSeatRatingSummariesByAuditoriumId(auditorium.getId());

            //then
            SeatRatingSummaryResponse response = summaries.stream()
                    .filter(s -> s.seatId().equals(seat1.getId()))
                    .findFirst()
                    .orElse(null);

            Assertions.assertNotNull(response);
            Assertions.assertEquals(response.row(),seat1.getRow());
            Assertions.assertEquals(response.column(), seat1.getColumn());
            Assertions.assertEquals(response.type(), SeatType.NO_REVIEW);
            Assertions.assertEquals(response.averageRating(), 0.0);
            Assertions.assertEquals(response.totalReviews(), 0);

        }
    }

    @Nested
    @DisplayName("[좌석 조회] 후기가 있는 좌석")
    class GetSeatRating {

        @Test
        @DisplayName("[happy] 후기가 있는 경우, 평점이 1.5~ 4.5 사이인 경우 중, 1.5의 케이스")
        public void normalReview_1_5() throws Exception {
            //given

            /// 리뷰 작성
            var request = ReviewRequest.builder()
                    .seatIds(List.of(seat1.getId()))
                    .content("test")
                    .movieTitle("ReviewTestTitle")
                    .imageUrls(null)
                    .rating(1.5)
                    .hashtags(List.of(hashTag1.getId(), hashTag2.getId(), hashTag3.getId()))
                    .build();
            reviewService.createReview(request, user1.getId());


            //when
            List<SeatRatingSummaryResponse> summaries = sut.getSeatRatingSummariesByAuditoriumId(auditorium.getId());

            //then
            SeatRatingSummaryResponse response = summaries.stream()
                    .filter(s -> s.seatId().equals(seat1.getId()))
                    .findFirst()
                    .orElse(null);

            Assertions.assertNotNull(response);
            Assertions.assertEquals(response.row(),seat1.getRow());
            Assertions.assertEquals(response.column(), seat1.getColumn());
            Assertions.assertEquals(response.type(), SeatType.REVIEWED);
            Assertions.assertEquals(response.averageRating(), 1.5, 0.001);
            Assertions.assertEquals(response.totalReviews(), 1);
        }

        @Test
        @DisplayName("[happy] 후기가 있는 경우, 평점이 1.5~ 4.5 사이인 경우 중, 4.5의 케이스")
        public void normalReview_4_5() throws Exception {
            //given

            /// 리뷰 작성
            var request = ReviewRequest.builder()
                    .seatIds(List.of(seat1.getId()))
                    .content("test")
                    .movieTitle("ReviewTestTitle")
                    .imageUrls(null)
                    .rating(4.5)
                    .hashtags(List.of(hashTag1.getId(), hashTag2.getId(), hashTag3.getId()))
                    .build();
            reviewService.createReview(request, user1.getId());


            //when
            List<SeatRatingSummaryResponse> summaries = sut.getSeatRatingSummariesByAuditoriumId(auditorium.getId());

            //then
            SeatRatingSummaryResponse response = summaries.stream()
                    .filter(s -> s.seatId().equals(seat1.getId()))
                    .findFirst()
                    .orElse(null);

            Assertions.assertNotNull(response);
            Assertions.assertEquals(response.row(),seat1.getRow());
            Assertions.assertEquals(response.column(), seat1.getColumn());
            Assertions.assertEquals(response.type(), SeatType.REVIEWED);
            Assertions.assertEquals(response.averageRating(), 4.5, 0.001);
            Assertions.assertEquals(response.totalReviews(), 1);
        }


        @Test
        @DisplayName("[happy] 여러 개의 후기가 있는 경우, 평균 평점과 개수 계산 확인")
        public void multiple_Review_calculate() throws Exception {
            //given
            var request1 = ReviewRequest.builder()
                    .seatIds(List.of(seat1.getId()))
                    .content("review1")
                    .movieTitle("title1")
                    .imageUrls(null)
                    .rating(4.3)
                    .hashtags(List.of(hashTag1.getId(), hashTag2.getId(), hashTag3.getId()))
                    .build();

            var request2 = ReviewRequest.builder()
                    .seatIds(List.of(seat1.getId()))
                    .content("review2")
                    .movieTitle("title2")
                    .imageUrls(null)
                    .rating(2.1)
                    .hashtags(List.of(hashTag1.getId(), hashTag2.getId(), hashTag3.getId()))
                    .build();

            reviewService.createReview(request1, user1.getId());
            reviewService.createReview(request2, user2.getId());

            //when
            List<SeatRatingSummaryResponse> summaries = sut.getSeatRatingSummariesByAuditoriumId(auditorium.getId());

            //then
            SeatRatingSummaryResponse response = summaries.stream()
                    .filter(s -> s.seatId().equals(seat1.getId()))
                    .findFirst()
                    .orElse(null);

            Assertions.assertNotNull(response);
            Assertions.assertEquals(3.2, response.averageRating(), 0.001); // (4 + 2) / 2
            Assertions.assertEquals(2, response.totalReviews());
            Assertions.assertEquals(SeatType.REVIEWED, response.type());
        }


        @Test
        @DisplayName("[happy] 후기가 있는 경우, 평점 낮은 좌석 (1.5 이하)")
        public void badReview() throws Exception {
            /// 리뷰 작성
            var request = ReviewRequest.builder()
                    .seatIds(List.of(seat1.getId()))
                    .content("test")
                    .movieTitle("ReviewTestTitle")
                    .imageUrls(null)
                    .rating(1.4)
                    .hashtags(List.of(hashTag1.getId(), hashTag2.getId(), hashTag3.getId()))
                    .build();
            reviewService.createReview(request, user1.getId());


            //when
            List<SeatRatingSummaryResponse> summaries = sut.getSeatRatingSummariesByAuditoriumId(auditorium.getId());

            //then
            SeatRatingSummaryResponse response = summaries.stream()
                    .filter(s -> s.seatId().equals(seat1.getId()))
                    .findFirst()
                    .orElse(null);

            Assertions.assertNotNull(response);
            Assertions.assertEquals(response.row(),seat1.getRow());
            Assertions.assertEquals(response.column(), seat1.getColumn());
            Assertions.assertEquals(response.type(), SeatType.LOW_RATED);
            Assertions.assertEquals(response.averageRating(), 1.4, 0.001);
            Assertions.assertEquals(response.totalReviews(), 1);
        }

        @Test
        @DisplayName("[happy] 후기가 있는 경우, 평점 높은 좌석 (4.5 이상)")
        public void highReview() throws Exception {

            //given

            /// 리뷰 작성
            var request = ReviewRequest.builder()
                    .seatIds(List.of(seat1.getId()))
                    .content("test")
                    .movieTitle("ReviewTestTitle")
                    .imageUrls(null)
                    .rating(4.6)
                    .hashtags(List.of(hashTag1.getId(), hashTag2.getId(), hashTag3.getId()))
                    .build();
            reviewService.createReview(request, user1.getId());


            //when
            List<SeatRatingSummaryResponse> summaries = sut.getSeatRatingSummariesByAuditoriumId(auditorium.getId());

            //then
            SeatRatingSummaryResponse response = summaries.stream()
                    .filter(s -> s.seatId().equals(seat1.getId()))
                    .findFirst()
                    .orElse(null);

            Assertions.assertNotNull(response);
            Assertions.assertEquals(response.row(),seat1.getRow());
            Assertions.assertEquals(response.column(), seat1.getColumn());
            Assertions.assertEquals(response.type(), SeatType.HIGH_RATED);
            Assertions.assertEquals(response.averageRating(), 4.6, 0.001);
            Assertions.assertEquals(response.totalReviews(), 1);
        }
    }

}
