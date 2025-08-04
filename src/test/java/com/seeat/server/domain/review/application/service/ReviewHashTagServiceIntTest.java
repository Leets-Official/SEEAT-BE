package com.seeat.server.domain.review.application.service;

import com.seeat.server.domain.hashtag.application.dto.response.AuditoriumHashTagResponse;
import com.seeat.server.domain.hashtag.application.usecase.ReviewHashTagUseCase;
import com.seeat.server.domain.review.domain.HashTagFixtures;
import com.seeat.server.domain.review.domain.ReviewFixtures;
import com.seeat.server.domain.hashtag.domain.entity.HashTag;
import com.seeat.server.domain.hashtag.domain.entity.HashTagType;
import com.seeat.server.domain.review.domain.entity.Review;
import com.seeat.server.domain.hashtag.domain.entity.ReviewHashTag;
import com.seeat.server.domain.hashtag.domain.repository.HashTagRepository;
import com.seeat.server.domain.hashtag.domain.repository.ReviewHashTagRepository;
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
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
class ReviewHashTagServiceIntTest {

    @Autowired
    private ReviewHashTagUseCase sut;

    @Autowired
    private ReviewHashTagRepository repository;

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
    private HashTagRepository hashTagRepository;

    @Autowired
    private ReviewRepository reviewRepository;

    /// 기타
    private Seat seat;
    private User user;
    private Theater theater;
    private Auditorium auditorium;
    private Review review;

    private HashTag hashTag1;
    private HashTag hashTag2;
    private HashTag hashTag3;
    private HashTag hashTag4;
    private HashTag hashTag5;

    @BeforeEach()
    void setUp() {
        theater = theaterRepository.save(TheaterFixtures.createTheater());
        auditorium = auditoriumRepository.save(AuditoriumFixtures.createAuditorium(theater));
        seat = seatRepository.save(SeatFixtures.createSeat(auditorium));
        user = userRepository.save(UserFixtures.createUser());
        review = reviewRepository.save(ReviewFixtures.createReview(user, seat));
        hashTag1 = hashTagRepository.save(HashTagFixtures.createHashTag(HashTagType.SOUND, "음향이 좋아요"));
        hashTag2 = hashTagRepository.save(HashTagFixtures.createHashTag(HashTagType.SOUND, "실감나는 음향이에요"));
        hashTag3 = hashTagRepository.save(HashTagFixtures.createHashTag(HashTagType.ENVIRONMENT, "좌석이 넓어요"));
        hashTag4 = hashTagRepository.save(HashTagFixtures.createHashTag(HashTagType.ENVIRONMENT, "통로가 넓어요"));
        hashTag5 = hashTagRepository.save(HashTagFixtures.createHashTag(HashTagType.COMPANION, "혼자 관람했어요"));

    }

    @Nested
    @DisplayName("생성 테스트")
    class CreateReviewHashTag {

        @Test
        @DisplayName("[happy] 정상적으로 생성되는지 체크")
        public void create_happy() throws Exception {

            //given
            Review review = reviewRepository.save(ReviewFixtures.createReview(user, seat, 1));

            //when
            /// 저장 대상 해시태그 구성
            sut.createReviewHashTag(review, List.of(hashTag1.getId(), hashTag3.getId(), hashTag5.getId()));

            //then
            List<ReviewHashTag> byReview = repository.findByReview(review);
            assertEquals(3, byReview.size());

            ReviewHashTag hashTagDB1 = byReview.get(0);
            assertEquals(hashTag1.getId(), hashTagDB1.getHashTag().getId());

            ReviewHashTag hashTagDB2 = byReview.get(1);
            assertEquals(hashTag3.getId(), hashTagDB2.getHashTag().getId());

            ReviewHashTag hashTagDB3 = byReview.get(2);
            assertEquals(hashTag5.getId(), hashTagDB3.getHashTag().getId());

        }


    }

    @Nested
    @DisplayName("조회 테스트")
    class FindReviewHashTag {

        @Test
        @DisplayName("[happy] 리뷰에서 사용된 해시태그가 정상적으로 상영관마다 개수와 함께 출력되는지 체크")
        public void find_happy() throws Exception {
            // given

            Review saved1 = reviewRepository.save(ReviewFixtures.createReview(user, seat, 1));
            Review saved2 = reviewRepository.save(ReviewFixtures.createReview(user, seat, 2));

            /// 저장 대상 해시태그 구성
            sut.createReviewHashTag(saved1, List.of(hashTag1.getId(), hashTag3.getId(), hashTag5.getId()));
            sut.createReviewHashTag(saved2, List.of(hashTag2.getId(), hashTag4.getId(), hashTag5.getId()));

            // when
            List<AuditoriumHashTagResponse> result = sut.loadReviewHashTagsByAuditoriumId(auditorium.getId());

            // then
            assertEquals(5, result.size());

            /// 1등 해시태그 조회
            AuditoriumHashTagResponse first = result.get(0);
            assertEquals(hashTag5.getId(), first.hashTagId());
            assertEquals(hashTag5.getName(), first.hashTagName());
            assertEquals(2, first.count());

            /// 개수가 동일할때는 ID가 큰 것 기준으로 조회
            AuditoriumHashTagResponse second = result.get(1);
            assertEquals(hashTag4.getId(), second.hashTagId());
            assertEquals(hashTag4.getName(), second.hashTagName());
            assertEquals(1, second.count());
        }
    }



}
