package com.seeat.server.domain.search.application.service;

import com.seeat.server.domain.review.domain.HashTagFixtures;
import com.seeat.server.domain.review.domain.ReviewFixtures;
import com.seeat.server.domain.review.domain.ReviewHashTagFixtures;
import com.seeat.server.domain.review.domain.ReviewLikeFixtures;
import com.seeat.server.domain.review.domain.entity.HashTag;
import com.seeat.server.domain.review.domain.entity.HashTagType;
import com.seeat.server.domain.review.domain.entity.Review;
import com.seeat.server.domain.review.domain.entity.ReviewLike;
import com.seeat.server.domain.review.domain.repository.HashTagRepository;
import com.seeat.server.domain.review.domain.repository.ReviewHashTagRepository;
import com.seeat.server.domain.review.domain.repository.ReviewLikeRepository;
import com.seeat.server.domain.review.domain.repository.ReviewRepository;
import com.seeat.server.domain.search.application.dto.request.ReviewSearchCondition;
import com.seeat.server.domain.search.application.dto.response.ReviewSearchResponse;
import com.seeat.server.domain.search.application.usecase.ReviewSearchUseCase;
import com.seeat.server.domain.search.domain.entity.SortType;
import com.seeat.server.domain.search.domain.repository.SearchRepository;
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
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;


@SpringBootTest
@ActiveProfiles("test")
@Transactional
public class ReviewSearchServiceTest {

    @Autowired
    private ReviewSearchUseCase sut;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private TheaterRepository theaterRepository;

    @Autowired
    private AuditoriumRepository auditoriumRepository;

    @Autowired
    private SeatRepository seatRepository;

    @Autowired
    private ReviewRepository reviewRepository;

    @Autowired
    private ReviewLikeRepository reviewLikeRepository;

    @Autowired
    private HashTagRepository hashTagRepository;

    @Autowired
    private ReviewHashTagRepository reviewHashTagRepository;

    @Nested
    @DisplayName("정렬 조건 리뷰 검색 테스트")
    class GetReviewListBySort {
        @Test
        @DisplayName("비회원 최신순 정렬 리뷰 검색 조회시 리스트 empty")
        void sortByLatest_AsGuest_Empty() {
            // given
            String guestToken = "guest-token-123";
            String keyword = "hello";

            User user = userRepository.save(UserFixtures.createUser());
            Theater theater = theaterRepository.save(TheaterFixtures.createTheater());
            Auditorium auditorium = auditoriumRepository.save(AuditoriumFixtures.createAuditorium(theater));
            Seat seat1 = seatRepository.save(SeatFixtures.createSeat(auditorium));

            Review review1 = reviewRepository.save(ReviewFixtures.createReview(user, seat1, 5, "test content1", "test1"));
            Review review2 = reviewRepository.save(ReviewFixtures.createReview(user, seat1, 4, "test content2", "test2"));
            Review review3 = reviewRepository.save(ReviewFixtures.createReview(user, seat1, 3, "content3", "3"));

            ReviewSearchCondition condition = ReviewSearchCondition.builder()
                    .keyword(keyword)
                    .sort(SortType.LATEST)
                    .build();

            var pageRequest = PageRequest.builder().page(1).size(10).build();

            // when
            SliceResponse<ReviewSearchResponse> response = sut.getReviewList(
                    condition,
                    guestToken,
                    null,
                    pageRequest
            );

            // then
            List<ReviewSearchResponse> responses = response.content();
            Assertions.assertThat(responses).hasSize(0);

            // 각 리뷰의 리스트가 empty 으로 반환되는지 검증
            Assertions.assertThat(response.content()).isNotNull();
        }

        @Test
        @DisplayName("비회원 최신순 정렬 리뷰 검색 조회")
        void sortByLatest_AsGuest_Success() {
            // given
            String guestToken = "guest-token-123";
            String keyword = "test";

            User user = userRepository.save(UserFixtures.createUser());
            Theater theater = theaterRepository.save(TheaterFixtures.createTheater());
            Auditorium auditorium = auditoriumRepository.save(AuditoriumFixtures.createAuditorium(theater));
            Seat seat1 = seatRepository.save(SeatFixtures.createSeat(auditorium));

            Review review1 = reviewRepository.save(ReviewFixtures.createReview(user, seat1, 5, "test content1", "test1"));
            Review review2 = reviewRepository.save(ReviewFixtures.createReview(user, seat1, 4, "test content2", "test2"));
            Review review3 = reviewRepository.save(ReviewFixtures.createReview(user, seat1, 3, "content3", "3"));

            ReviewSearchCondition condition = ReviewSearchCondition.builder()
                    .keyword(keyword)
                    .sort(SortType.LATEST)
                    .build();

            var pageRequest = PageRequest.builder().page(1).size(10).build();

            // when
            SliceResponse<ReviewSearchResponse> response = sut.getReviewList(
                    condition,
                    guestToken,
                    null,
                    pageRequest
            );

            // then
            List<ReviewSearchResponse> responses = response.content();
            Assertions.assertThat(responses).hasSize(2);

            // 각 리뷰의 리스트가 최신순으로 반환되는지 검증
            Assertions.assertThat(responses.get(0).content()).isEqualTo(review2.getContent());
            Assertions.assertThat(responses.get(1).content()).isEqualTo(review1.getContent());
        }

        @Test
        @DisplayName("비회원 평점순 정렬 리뷰 검색 조회")
        void sortByRating_AsGuest_Success() {
            // given
            String guestToken = "guest-token-123";
            String keyword = "test";

            User user = userRepository.save(UserFixtures.createUser());
            Theater theater = theaterRepository.save(TheaterFixtures.createTheater());
            Auditorium auditorium = auditoriumRepository.save(AuditoriumFixtures.createAuditorium(theater));
            Seat seat1 = seatRepository.save(SeatFixtures.createSeat(auditorium));

            Review review1 = reviewRepository.save(ReviewFixtures.createReview(user, seat1, 5, "test content1", "test1"));
            Review review2 = reviewRepository.save(ReviewFixtures.createReview(user, seat1, 4, "test content2", "test2"));
            Review review3 = reviewRepository.save(ReviewFixtures.createReview(user, seat1, 3, "content3", "3"));

            ReviewSearchCondition condition = ReviewSearchCondition.builder()
                    .keyword(keyword)
                    .sort(SortType.RATING)
                    .build();

            var pageRequest = PageRequest.builder().page(1).size(10).build();

            // when
            SliceResponse<ReviewSearchResponse> response = sut.getReviewList(
                    condition,
                    guestToken,
                    null,
                    pageRequest
            );

            // then
            List<ReviewSearchResponse> responses = response.content();
            Assertions.assertThat(responses).hasSize(2);

            // 각 리뷰의 리스트가 평준순으로 반환되는지 검증
            Assertions.assertThat(responses.get(0).content()).isEqualTo(review1.getContent());
            Assertions.assertThat(responses.get(1).content()).isEqualTo(review2.getContent());
        }

        @Test
        @DisplayName("비회원 인기순 정렬 리뷰 검색 조회")
        void sortByPopular_AsGuest_Success() {
            // given
            String guestToken = "guest-token-123";
            String keyword = "test";

            User user = userRepository.save(UserFixtures.createUser());
            Theater theater = theaterRepository.save(TheaterFixtures.createTheater());
            Auditorium auditorium = auditoriumRepository.save(AuditoriumFixtures.createAuditorium(theater));
            Seat seat1 = seatRepository.save(SeatFixtures.createSeat(auditorium));

            Review review1 = reviewRepository.save(ReviewFixtures.createReview(user, seat1, 5, "test content1", "test1"));
            Review review2 = reviewRepository.save(ReviewFixtures.createReview(user, seat1, 4, "test content2", "test2"));
            Review review3 = reviewRepository.save(ReviewFixtures.createReview(user, seat1, 3, "content3", "3"));

            ReviewLike like1 = reviewLikeRepository.save(ReviewLikeFixtures.stub(user, review1));
            ReviewLike like2 = reviewLikeRepository.save(ReviewLikeFixtures.stub(user, review1));
            ReviewLike like3 = reviewLikeRepository.save(ReviewLikeFixtures.stub(user, review2));

            ReviewSearchCondition condition = ReviewSearchCondition.builder()
                    .keyword(keyword)
                    .sort(SortType.RATING)
                    .build();

            var pageRequest = PageRequest.builder().page(1).size(10).build();

            // when
            SliceResponse<ReviewSearchResponse> response = sut.getReviewList(
                    condition,
                    guestToken,
                    null,
                    pageRequest
            );

            // then
            List<ReviewSearchResponse> responses = response.content();
            Assertions.assertThat(responses).hasSize(2);

            // 각 리뷰의 리스트가 인기순으로 반환되는지 검증
            Assertions.assertThat(responses.get(0).content()).isEqualTo(review1.getContent());
            Assertions.assertThat(responses.get(1).content()).isEqualTo(review2.getContent());
        }

    }

    @Nested
    @DisplayName("회원/비회원 리뷰 검색시 좋아요 여부 조회 테스트")
    class getUserLike {
        @Test
        @DisplayName("비회원 인기순 정렬 리뷰 검색 조회시 좋아요 누름 여부 false 확인")
        void likeStatusFalse_AsGuest() {
            // given
            String guestToken = "guest-token-123";
            String keyword = "test";

            User user = userRepository.save(UserFixtures.createUser());
            Theater theater = theaterRepository.save(TheaterFixtures.createTheater());
            Auditorium auditorium = auditoriumRepository.save(AuditoriumFixtures.createAuditorium(theater));
            Seat seat1 = seatRepository.save(SeatFixtures.createSeat(auditorium));

            Review review1 = reviewRepository.save(ReviewFixtures.createReview(user, seat1, 5, "test content1", "test1"));
            Review review2 = reviewRepository.save(ReviewFixtures.createReview(user, seat1, 4, "test content2", "test2"));
            Review review3 = reviewRepository.save(ReviewFixtures.createReview(user, seat1, 3, "content3", "3"));

            ReviewLike like1 = reviewLikeRepository.save(ReviewLikeFixtures.stub(user, review1));
            ReviewLike like2 = reviewLikeRepository.save(ReviewLikeFixtures.stub(user, review1));
            ReviewLike like3 = reviewLikeRepository.save(ReviewLikeFixtures.stub(user, review2));

            ReviewSearchCondition condition = ReviewSearchCondition.builder()
                    .keyword(keyword)
                    .sort(SortType.RATING)
                    .build();

            var pageRequest = PageRequest.builder().page(1).size(10).build();

            // when
            SliceResponse<ReviewSearchResponse> response = sut.getReviewList(
                    condition,
                    guestToken,
                    null,
                    pageRequest
            );

            // then
            List<ReviewSearchResponse> responses = response.content();
            Assertions.assertThat(responses).hasSize(2);

            // 좋아요 누름 여부가 모두 false 인지 검증
            for (ReviewSearchResponse r : responses) {
                Assertions.assertThat(r.likedByUser()).isFalse();
            }
        }

        @Test
        @DisplayName("회원 인기순 정렬 리뷰 검색 조회시 좋아요 누름 여부 true 확인")
        void likeStatusTrue_AsUser() {
            // given
            User user1 = userRepository.save(UserFixtures.createUser());
            Theater theater = theaterRepository.save(TheaterFixtures.createTheater());
            Auditorium auditorium = auditoriumRepository.save(AuditoriumFixtures.createAuditorium(theater));
            Seat seat1 = seatRepository.save(SeatFixtures.createSeat(auditorium));

            Review review1 = reviewRepository.save(ReviewFixtures.createReview(user1, seat1, 5, "test content1", "test1"));
            Review review2 = reviewRepository.save(ReviewFixtures.createReview(user1, seat1, 4, "test content2", "test2"));
            Review review3 = reviewRepository.save(ReviewFixtures.createReview(user1, seat1, 3, "content3", "3"));

            ReviewLike like1 = reviewLikeRepository.save(ReviewLikeFixtures.stub(user1, review1));
            ReviewLike like3 = reviewLikeRepository.save(ReviewLikeFixtures.stub(user1, review2));

            Long userId = user1.getId();
            String keyword = "test";

            ReviewSearchCondition condition = ReviewSearchCondition.builder()
                    .keyword(keyword)
                    .sort(SortType.RATING)
                    .build();

            var pageRequest = PageRequest.builder().page(1).size(10).build();

            // when
            SliceResponse<ReviewSearchResponse> response = sut.getReviewList(
                    condition,
                    null,
                    userId,
                    pageRequest
            );

            // then
            List<ReviewSearchResponse> responses = response.content();
            Assertions.assertThat(responses).hasSize(2);

            // 좋아요 누름 여부가 모두 true 인지 검증
            for (ReviewSearchResponse r : responses) {
                Assertions.assertThat(r.likedByUser()).isTrue();
            }
        }
    }

    @Nested
    @DisplayName("필터 조건 리뷰 검색 테스트")
    class GetReviewListByCondition {

        @Test
        @DisplayName("키워드 필터시 빈 키워드 입력시 리스트 empty")
        void filterByKeyword_All() {
            // given
            String guestToken = "guest-token-123";
            String keyword = "";

            User user = userRepository.save(UserFixtures.createUser());
            Theater theater = theaterRepository.save(TheaterFixtures.createTheater());
            Auditorium auditorium = auditoriumRepository.save(AuditoriumFixtures.createAuditorium(theater));
            Seat seat1 = seatRepository.save(SeatFixtures.createSeat(auditorium));

            Review review1 = reviewRepository.save(ReviewFixtures.createReview(user, seat1, 5, "test content1", "test1"));
            Review review2 = reviewRepository.save(ReviewFixtures.createReview(user, seat1, 4, "test content2", "test2"));
            Review review3 = reviewRepository.save(ReviewFixtures.createReview(user, seat1, 3, "content3", "3"));

            ReviewSearchCondition condition = ReviewSearchCondition.builder()
                    .keyword(keyword)
                    .sort(SortType.LATEST)
                    .build();

            var pageRequest = PageRequest.builder().page(1).size(10).build();

            // when
            SliceResponse<ReviewSearchResponse> response = sut.getReviewList(
                    condition,
                    guestToken,
                    null,
                    pageRequest
            );

            // then
            List<ReviewSearchResponse> responses = response.content();
            Assertions.assertThat(responses).hasSize(3);

            // 각 리뷰의 리스트가 전체 반환되는지 검증
            Assertions.assertThat(responses.get(0).content()).isEqualTo(review3.getContent());
            Assertions.assertThat(responses.get(1).content()).isEqualTo(review2.getContent());
            Assertions.assertThat(responses.get(2).content()).isEqualTo(review1.getContent());
        }

        @Test
        @DisplayName("상영관 필터시 특정 auditoriumId 리뷰만 조회")
        void filterByAuditoriumId_Success() {
            // given
            String guestToken = "guest-token-123";
            String keyword = "test";

            User user = userRepository.save(UserFixtures.createUser());
            Theater theater = theaterRepository.save(TheaterFixtures.createTheater());
            Auditorium auditorium1 = auditoriumRepository.save(AuditoriumFixtures.createAuditorium(theater));
            Auditorium auditorium2 = auditoriumRepository.save(AuditoriumFixtures.createAuditorium(theater));
            Seat seat1 = seatRepository.save(SeatFixtures.createSeat(auditorium1));
            Seat seat2 = seatRepository.save(SeatFixtures.createSeat(auditorium2));

            Review review1 = reviewRepository.save(ReviewFixtures.createReview(user, seat1, 5, "test content1", "test1"));
            Review review2 = reviewRepository.save(ReviewFixtures.createReview(user, seat2, 4, "test content2", "test2"));
            Review review3 = reviewRepository.save(ReviewFixtures.createReview(user, seat2, 3, "content3", "3"));

            ReviewSearchCondition condition = ReviewSearchCondition.builder()
                    .keyword(keyword)
                    .auditoriumId(auditorium1.getId())
                    .sort(SortType.LATEST)
                    .build();

            var pageRequest = PageRequest.builder().page(1).size(10).build();

            // when
            SliceResponse<ReviewSearchResponse> response = sut.getReviewList(
                    condition,
                    guestToken,
                    null,
                    pageRequest
            );

            // then
            List<ReviewSearchResponse> responses = response.content();
            Assertions.assertThat(responses).hasSize(1);

            // 각 리뷰의 리스트가 상영관 필터로 반환되는지 검증
            Assertions.assertThat(responses.get(0).content()).isEqualTo(review1.getContent());
        }

        @Test
        @DisplayName("상영관 필터시 존재하지 않는 auditoriumId 입력")
        void filterByAuditoriumId_NotExist() {
            // given
            String guestToken = "guest-token-123";
            String keyword = "test";

            User user = userRepository.save(UserFixtures.createUser());
            Theater theater = theaterRepository.save(TheaterFixtures.createTheater());
            Auditorium auditorium1 = auditoriumRepository.save(AuditoriumFixtures.createAuditorium(theater));
            Auditorium auditorium2 = auditoriumRepository.save(AuditoriumFixtures.createAuditorium(theater));
            Seat seat1 = seatRepository.save(SeatFixtures.createSeat(auditorium1));
            Seat seat2 = seatRepository.save(SeatFixtures.createSeat(auditorium2));

            Review review1 = reviewRepository.save(ReviewFixtures.createReview(user, seat1, 5, "test content1", "test1"));
            Review review2 = reviewRepository.save(ReviewFixtures.createReview(user, seat2, 4, "test content2", "test2"));
            Review review3 = reviewRepository.save(ReviewFixtures.createReview(user, seat2, 3, "content3", "3"));

            ReviewSearchCondition condition = ReviewSearchCondition.builder()
                    .keyword(keyword)
                    .auditoriumId("1")
                    .sort(SortType.LATEST)
                    .build();

            var pageRequest = PageRequest.builder().page(1).size(10).build();

            // when
            SliceResponse<ReviewSearchResponse> response = sut.getReviewList(
                    condition,
                    guestToken,
                    null,
                    pageRequest
            );

            // then
            List<ReviewSearchResponse> responses = response.content();
            Assertions.assertThat(responses).hasSize(0);

            // 각 리뷰의 리스트가 empty 로 반환되는지 검증
            Assertions.assertThat(response.content()).isNotNull();
        }

        @Test
        @DisplayName("해시태그 필터시 특정 해시태그 포함 리뷰만 조회")
        void filterByHashTags_Success() {
            // given
            String guestToken = "guest-token-123";
            String keyword = "test";

            User user = userRepository.save(UserFixtures.createUser());
            Theater theater = theaterRepository.save(TheaterFixtures.createTheater());
            Auditorium auditorium = auditoriumRepository.save(AuditoriumFixtures.createAuditorium(theater));
            Seat seat1 = seatRepository.save(SeatFixtures.createSeat(auditorium));

            HashTag hashTag1 = hashTagRepository.save(HashTagFixtures.createHashTag(HashTagType.COMPANION, "자녀와"));
            HashTag hashTag2 = hashTagRepository.save(HashTagFixtures.createHashTag(HashTagType.COMPANION, "부모와"));
            List<Long> hashTagIds = List.of(hashTag1.getId());

            Review review1 = reviewRepository.save(ReviewFixtures.createReview(user, seat1, 5, "test content1", "test1"));
            Review review2 = reviewRepository.save(ReviewFixtures.createReview(user, seat1, 4, "test content2", "test2"));
            Review review3 = reviewRepository.save(ReviewFixtures.createReview(user, seat1, 3, "content3", "3"));

            reviewHashTagRepository.save(ReviewHashTagFixtures.createReviewHashTag(review1, hashTag1));
            reviewHashTagRepository.save(ReviewHashTagFixtures.createReviewHashTag(review3, hashTag1));

            ReviewSearchCondition condition = ReviewSearchCondition.builder()
                    .keyword(keyword)
                    .sort(SortType.LATEST)
                    .hashTagIds(hashTagIds)
                    .build();

            var pageRequest = PageRequest.builder().page(1).size(10).build();

            // when
            SliceResponse<ReviewSearchResponse> response = sut.getReviewList(
                    condition,
                    guestToken,
                    null,
                    pageRequest
            );

            // then
            List<ReviewSearchResponse> responses = response.content();
            Assertions.assertThat(responses).hasSize(1);

            // 각 리뷰의 리스트가 해시태그 필터로 반환되는지 검증
            Assertions.assertThat(responses.get(0).content()).isEqualTo(review1.getContent());
        }

        @Test
        @DisplayName("해시태그 필터시 존재하지 않는 해시태그 Id 입력")
        void filterByHashTags_NotExist() {
            // given
            String guestToken = "guest-token-123";
            String keyword = "test";

            User user = userRepository.save(UserFixtures.createUser());
            Theater theater = theaterRepository.save(TheaterFixtures.createTheater());
            Auditorium auditorium = auditoriumRepository.save(AuditoriumFixtures.createAuditorium(theater));
            Seat seat1 = seatRepository.save(SeatFixtures.createSeat(auditorium));

            HashTag hashTag1 = hashTagRepository.save(HashTagFixtures.createHashTag(HashTagType.COMPANION, "자녀와"));
            HashTag hashTag2 = hashTagRepository.save(HashTagFixtures.createHashTag(HashTagType.COMPANION, "부모와"));
            List<Long> hashTagIds = List.of(hashTag2.getId());

            Review review1 = reviewRepository.save(ReviewFixtures.createReview(user, seat1, 5, "test content1", "test1"));
            Review review2 = reviewRepository.save(ReviewFixtures.createReview(user, seat1, 4, "test content2", "test2"));
            Review review3 = reviewRepository.save(ReviewFixtures.createReview(user, seat1, 3, "content3", "3"));

            reviewHashTagRepository.save(ReviewHashTagFixtures.createReviewHashTag(review1, hashTag1));
            reviewHashTagRepository.save(ReviewHashTagFixtures.createReviewHashTag(review3, hashTag1));

            ReviewSearchCondition condition = ReviewSearchCondition.builder()
                    .keyword(keyword)
                    .sort(SortType.LATEST)
                    .hashTagIds(hashTagIds)
                    .build();

            var pageRequest = PageRequest.builder().page(1).size(10).build();

            // when
            SliceResponse<ReviewSearchResponse> response = sut.getReviewList(
                    condition,
                    guestToken,
                    null,
                    pageRequest
            );

            // then
            List<ReviewSearchResponse> responses = response.content();
            Assertions.assertThat(responses).hasSize(0);

            // 각 리뷰의 리스트가 empty 로 반환되는지 검증
            Assertions.assertThat(response.content()).isNotNull();
        }

    }
}
