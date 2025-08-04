package com.seeat.server.domain.user.application.service;

import com.seeat.server.domain.hashtag.domain.entity.HashTag;
import com.seeat.server.domain.hashtag.domain.entity.HashTagType;
import com.seeat.server.domain.hashtag.domain.repository.HashTagRepository;
import com.seeat.server.domain.review.application.dto.request.ReviewRequest;
import com.seeat.server.domain.review.application.service.ReviewService;
import com.seeat.server.domain.review.domain.HashTagFixtures;
import com.seeat.server.domain.review.domain.ReviewLikeFixtures;
import com.seeat.server.domain.review.domain.entity.Review;
import com.seeat.server.domain.review.domain.repository.ReviewLikeRepository;
import com.seeat.server.domain.review.domain.repository.ReviewRepository;
import com.seeat.server.domain.theater.domain.AuditoriumFixtures;
import com.seeat.server.domain.theater.domain.SeatFixtures;
import com.seeat.server.domain.theater.domain.TheaterFixtures;
import com.seeat.server.domain.theater.application.dto.response.AuditoriumResponse;
import com.seeat.server.domain.theater.domain.entity.Auditorium;
import com.seeat.server.domain.theater.domain.entity.MovieGenre;
import com.seeat.server.domain.theater.domain.entity.Seat;
import com.seeat.server.domain.theater.domain.entity.Theater;
import com.seeat.server.domain.theater.domain.repository.AuditoriumRepository;
import com.seeat.server.domain.theater.domain.repository.SeatRepository;
import com.seeat.server.domain.theater.domain.repository.TheaterRepository;
import com.seeat.server.domain.user.application.dto.request.UserInfoUpdateRequest;
import com.seeat.server.domain.user.application.dto.response.UserGradeResponse;
import com.seeat.server.domain.user.application.dto.response.UserInfoResponse;
import com.seeat.server.domain.user.application.dto.response.UserInfoUpdateResponse;
import com.seeat.server.domain.user.domain.UserFixtures;
import com.seeat.server.domain.user.domain.entity.User;
import com.seeat.server.domain.user.domain.entity.UserAuditorium;
import com.seeat.server.domain.user.domain.entity.UserGrade;
import com.seeat.server.domain.user.domain.repository.UserAuditoriumRepository;
import com.seeat.server.domain.user.domain.repository.UserRepository;
import com.seeat.server.global.response.ErrorCode;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
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

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertIterableEquals;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
public class UserProfileServiceTest {

    @Autowired
    private UserProfileService sut;

    @Autowired
    private UserRepository repository;

    @Autowired
    private AuditoriumRepository auditoriumRepository;

    @Autowired
    private TheaterRepository theaterRepository;

    @Autowired
    private UserAuditoriumRepository userAuditoriumRepository;

    @Autowired
    private SeatRepository seatRepository;

    @Autowired
    private ReviewRepository reviewRepository;

    @Autowired
    private ReviewLikeRepository reviewLikeRepository;

    @Autowired
    private HashTagRepository hashTagRepository;

    @Autowired
    private ReviewService reviewService;

    private Theater theater;
    private Auditorium auditorium;
    private Seat seat1;
    private Seat seat2;
    private User user1;
    private User user2;
    private User user3;
    private User user4;
    private HashTag hashTag1;
    private HashTag hashTag2;
    private HashTag hashTag3;


    @BeforeEach
    void setUp() throws IOException {
        theater = theaterRepository.save(TheaterFixtures.createTheater());
        auditorium = auditoriumRepository.save(AuditoriumFixtures.createAuditorium(theater));
        seat1 = seatRepository.save(SeatFixtures.createSeat(auditorium));
        seat2 = seatRepository.save(SeatFixtures.createSeat(auditorium));
        user1 = repository.save(UserFixtures.createUser("user1@test.com"));
        user2 = repository.save(UserFixtures.createUser("user2@test.com"));
        user3 = repository.save(UserFixtures.createUser("user3@test.com"));
        user4 = repository.save(UserFixtures.createUser("user4@test.com"));
        hashTag1 = hashTagRepository.save(HashTagFixtures.createHashTag(HashTagType.SOUND, "해시태그 1"));
        hashTag2 = hashTagRepository.save(HashTagFixtures.createHashTag(HashTagType.COMPANION, "해시태그 2"));
        hashTag3 = hashTagRepository.save(HashTagFixtures.createHashTag(HashTagType.ENVIRONMENT, "해시태그 3"));
    }

    @Nested
    @DisplayName("사용자 정보 조회 테스트")
    class getUserInfo{

        @Test
        @DisplayName("로그인한 유저 정보 정상 조회")
        void getUserInfo_Success() throws IOException {
            // given
            userAuditoriumRepository.save(UserAuditorium.of(user1, auditorium));

            Review review1 = reviewService.createReview(getReviewRequest(List.of(seat1, seat2), 5, "test-1"), user1.getId());
            Review review2 = reviewService.createReview(getReviewRequest(List.of(seat1, seat2), 5, "test-1"), user1.getId());

            /// 좋아요
            reviewLikeRepository.saveAll(List.of(
                    ReviewLikeFixtures.stub(user2, review1),
                    ReviewLikeFixtures.stub(user3, review1),
                    ReviewLikeFixtures.stub(user2, review1),
                    ReviewLikeFixtures.stub(user3, review2),
                    ReviewLikeFixtures.stub(user4, review2)
            ));


            // when
            UserInfoResponse response = sut.getUserInfo(user1.getId());


            // then
            assertEquals(user1.getId(), response.id());
            assertEquals(user1.getEmail(), response.email());
            assertEquals(user1.getSocialId(), response.socialId());
            assertEquals(user1.getUsername(), response.username());
            assertEquals(user1.getImageUrl(), response.imageUrl());
            assertEquals(user1.getNickname(), response.nickname());
            assertEquals(user1.getGenres(), response.genres());
            assertEquals(user1.getSocial(), response.social());
            List<AuditoriumResponse> auditoriumResponse = List.of(AuditoriumResponse.from(auditorium));
            assertEquals(auditoriumResponse, response.auditoriums());
            assertEquals(2, response.reviewCount());
            assertEquals(5, response.likeCount());
            assertEquals(UserGrade.SILVER, response.grade());
            double expectedExp = (Math.min((double) 2 / 2, 1.0) + Math.min((double) 5 / 5, 1.0)) / 2 * 100;
            assertEquals(expectedExp, response.levelExp(), 0.0001);
        }

        @Test
        @DisplayName("로그인한 유저 정보 조회 예외 발생")
        void getUserInfo_Fail() {
            // given
            User user = UserFixtures.fakeUser();

            // when & then
            Assertions.assertThatThrownBy(() -> sut.getUserInfo(user.getId()))
                    .isInstanceOf(NoSuchElementException.class)
                    .hasMessageContaining(ErrorCode.NOT_USER.getMessage());
        }

        @Test
        @DisplayName("상영관 정보 조회 예외 발생")
        void getUserInfoAuditorium_Fail() {
            // given
            User user = UserFixtures.createUser();
            repository.save(user);

            // when & then
            Assertions.assertThatThrownBy(() -> sut.getUserInfo(user.getId()))
                    .isInstanceOf(NoSuchElementException.class)
                    .hasMessageContaining(ErrorCode.NOT_AUDITORIUM.getMessage());
        }
    }

    @Nested
    @DisplayName("사용자 정보 수정 테스트")
    class updateUserInfo {
        @Test
        @DisplayName("로그인한 유저 정보 정상 수정")
        void updateUserInfo_Success() throws IOException {
            // given
            User user = UserFixtures.createUser();
            Theater theater1 = TheaterFixtures.createTheater();
            Theater theater2 = TheaterFixtures.createTheater();
            repository.save(user);
            theaterRepository.save(theater1);
            theaterRepository.save(theater2);
            Auditorium auditorium1 = AuditoriumFixtures.createAuditorium(theater1, "audTest1");
            Auditorium auditorium2 = AuditoriumFixtures.createAuditorium(theater2, "audTest2");
            auditoriumRepository.save(auditorium1);
            auditoriumRepository.save(auditorium2);
            userAuditoriumRepository.save(UserAuditorium.of(user, auditorium1));

            // 수정 정보
            String newNickname = "updateNick";
            List<MovieGenre> newGenres = List.of(MovieGenre.COMEDY, MovieGenre.HORROR);
            List<String> auditoriumIds = List.of("audTest2");
            UserInfoUpdateRequest request = new UserInfoUpdateRequest(newNickname, "file1.jpg", newGenres, auditoriumIds);

            // when
            UserInfoUpdateResponse response = sut.updateUserInfo(user.getId(), request);

            // then
            assertEquals(newNickname, response.nickname());
            String thumbnailUrl = user.getImageUrl();
            Assertions.assertThat(thumbnailUrl.contains("file1"));

            assertIterableEquals(newGenres, response.genres());
            List<AuditoriumResponse> auditoriumResponse = List.of(AuditoriumResponse.from(auditorium2));
            assertEquals(auditoriumResponse, response.auditoriums());
        }

        @Test
        @DisplayName("로그인한 유저 사용자 예외 발생")
        void updateUserInfo_Fail() {
            // given
            User user = UserFixtures.fakeUser();

            // 수정 정보
            String newNickname = "updateNick";
            List<MovieGenre> newGenres = List.of(MovieGenre.COMEDY, MovieGenre.HORROR);
            List<String> auditoriumIds = List.of("aud1");
            UserInfoUpdateRequest request = new UserInfoUpdateRequest(newNickname, "file1.jpg", newGenres, auditoriumIds);

            // when & then
            Assertions.assertThatThrownBy(() -> sut.updateUserInfo(user.getId(), request))
                    .isInstanceOf(NoSuchElementException.class)
                    .hasMessageContaining(ErrorCode.NOT_USER.getMessage());
        }

        @Test
        @DisplayName("로그인한 유저 정보 수정 상영관 예외 발생")
        void updateUserInfoAuditorium_Fail() {
            // given
            User user = UserFixtures.createUser();
            repository.save(user);

            // 수정 정보
            String newNickname = "updateNick";
            List<MovieGenre> newGenres = List.of(MovieGenre.COMEDY, MovieGenre.HORROR);
            List<String> auditoriumIds = List.of("aud1");
            UserInfoUpdateRequest request = new UserInfoUpdateRequest(newNickname, "file1.jpg", newGenres, auditoriumIds);

            // when & then
            Assertions.assertThatThrownBy(() -> sut.updateUserInfo(user.getId(), request))
                    .isInstanceOf(NoSuchElementException.class)
                    .hasMessageContaining(ErrorCode.NOT_AUDITORIUM.getMessage());
        }
    }

    @Test
    @DisplayName("사용자 등급 목록 조회 테스트")
    void getUserGradeList() {
        // when
        List<UserGradeResponse> responses = sut.getUserGradeList();

        // then
        assertThat(responses)
                .extracting(UserGradeResponse::grade)
                .containsExactly(UserGrade.BRONZE, UserGrade.SILVER, UserGrade.GOLD, UserGrade.PLATINUM);
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
}
