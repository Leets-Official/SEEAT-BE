package com.seeat.server.domain.search.application.service;

import com.seeat.server.domain.review.application.dto.request.ReviewRequest;
import com.seeat.server.domain.review.application.service.ReviewService;
import com.seeat.server.domain.review.domain.HashTagFixtures;
import com.seeat.server.domain.review.domain.ReviewLikeFixtures;
import com.seeat.server.domain.hashtag.domain.entity.HashTag;
import com.seeat.server.domain.hashtag.domain.entity.HashTagType;
import com.seeat.server.domain.review.domain.entity.Review;
import com.seeat.server.domain.review.domain.entity.ReviewLike;
import com.seeat.server.domain.hashtag.domain.repository.HashTagRepository;
import com.seeat.server.domain.hashtag.domain.repository.ReviewHashTagRepository;
import com.seeat.server.domain.review.domain.repository.ReviewLikeRepository;
import com.seeat.server.domain.search.application.dto.request.ReviewSearchCondition;
import com.seeat.server.domain.search.application.dto.response.ReviewSearchResponse;
import com.seeat.server.domain.search.application.usecase.ReviewSearchUseCase;
import com.seeat.server.domain.search.domain.entity.SortType;
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
    private ReviewLikeRepository reviewLikeRepository;

    @Autowired
    private HashTagRepository hashTagRepository;

    @Autowired
    private ReviewHashTagRepository reviewHashTagRepository;

    /// 리뷰는 직접 작성해야한다.
    @Autowired
    private ReviewService reviewService;

    private Theater theater;
    private Auditorium auditorium1;
    private Auditorium auditorium2;
    private Seat seat1;
    private Seat seat2;
    private Seat seat3;
    private User user1;
    private HashTag hashTag1;
    private HashTag hashTag2;
    private HashTag hashTag3;
    private HashTag hashTag4;
    private HashTag hashTag5;
    private HashTag hashTag6;

    @BeforeEach
    void setUp() throws IOException {
        theater = theaterRepository.save(TheaterFixtures.createTheater());
        auditorium1 = auditoriumRepository.save(AuditoriumFixtures.createAuditorium(theater));
        seat1 = seatRepository.save(SeatFixtures.createSeat(auditorium1));
        seat2 = seatRepository.save(SeatFixtures.createSeat(auditorium1));
        seat3 = seatRepository.save(SeatFixtures.createSeat(auditorium2));
        user1 = userRepository.save(UserFixtures.createUser());
        hashTag1 = hashTagRepository.save(HashTagFixtures.createHashTag(HashTagType.SOUND, "해시태그 1"));
        hashTag2 = hashTagRepository.save(HashTagFixtures.createHashTag(HashTagType.COMPANION, "해시태그 2"));
        hashTag3 = hashTagRepository.save(HashTagFixtures.createHashTag(HashTagType.ENVIRONMENT, "해시태그 3"));
        hashTag4 = hashTagRepository.save(HashTagFixtures.createHashTag(HashTagType.SOUND, "해시태그 4"));
        hashTag5 = hashTagRepository.save(HashTagFixtures.createHashTag(HashTagType.COMPANION, "해시태그 5"));
        hashTag6 = hashTagRepository.save(HashTagFixtures.createHashTag(HashTagType.ENVIRONMENT, "해시태그 6"));
    }

    @Nested
    @DisplayName("정렬 조건 리뷰 검색 테스트")
    class GetReviewListBySort {
        @Test
        @DisplayName("비회원 최신순 정렬 리뷰 검색 조회시 리스트 empty")
        void sortByLatest_AsGuest_Empty() throws IOException {
            // given
            String guestToken = "guest-token-123";
            String keyword = "hello";

            Review review1 = reviewService.createReview(getReviewRequest(List.of(seat1, seat2), 5, "test-1"), user1.getId());
            Review review2 = reviewService.createReview(getReviewRequest(List.of(seat1), 4, "test-2"), user1.getId());
            Review review3 = reviewService.createReview(getReviewRequest(List.of(seat2), 3, "test-3"), user1.getId());

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
        void sortByLatest_AsGuest_Success() throws IOException {
            // given
            String guestToken = "guest-token-123";
            String keyword = "test";

            Review review1 = reviewService.createReview(getReviewRequest(List.of(seat1, seat2), 5, "test-1"), user1.getId());
            Review review2 = reviewService.createReview(getReviewRequest(List.of(seat1), 4, "test-2"), user1.getId());

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
        void sortByRating_AsGuest_Success() throws IOException {
            // given
            String guestToken = "guest-token-123";
            String keyword = "test";

            Review review1 = reviewService.createReview(getReviewRequest(List.of(seat1, seat2), 5, "test-1"), user1.getId());
            Review review2 = reviewService.createReview(getReviewRequest(List.of(seat1), 4, "test-2"), user1.getId());

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
        void sortByPopular_AsGuest_Success() throws IOException {
            // given
            String guestToken = "guest-token-123";
            String keyword = "test";

            Review review1 = reviewService.createReview(getReviewRequest(List.of(seat1, seat2), 5, "test-1"), user1.getId());
            Review review2 = reviewService.createReview(getReviewRequest(List.of(seat1), 4, "test-2"), user1.getId());
            Review review3 = reviewService.createReview(getReviewRequest(List.of(seat2), 3, "test-3"), user1.getId());

            ReviewLike like1 = reviewLikeRepository.save(ReviewLikeFixtures.stub(user1, review1));
            ReviewLike like2 = reviewLikeRepository.save(ReviewLikeFixtures.stub(user1, review1));
            ReviewLike like3 = reviewLikeRepository.save(ReviewLikeFixtures.stub(user1, review2));

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
            Assertions.assertThat(responses).hasSize(3);

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
        void likeStatusFalse_AsGuest() throws IOException {
            // given
            String guestToken = "guest-token-123";
            String keyword = "test";

            Review review1 = reviewService.createReview(getReviewRequest(List.of(seat1, seat2), 5, "test-1"), user1.getId());
            Review review2 = reviewService.createReview(getReviewRequest(List.of(seat1), 4, "test-2"), user1.getId());

            ReviewLike like1 = reviewLikeRepository.save(ReviewLikeFixtures.stub(user1, review1));
            ReviewLike like2 = reviewLikeRepository.save(ReviewLikeFixtures.stub(user1, review1));
            ReviewLike like3 = reviewLikeRepository.save(ReviewLikeFixtures.stub(user1, review2));

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
        void likeStatusTrue_AsUser() throws IOException {
            // given
            Review review1 = reviewService.createReview(getReviewRequest(List.of(seat1, seat2), 5, "test-1"), user1.getId());
            Review review2 = reviewService.createReview(getReviewRequest(List.of(seat1), 4, "test-2"), user1.getId());

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
        void filterByKeyword_All() throws IOException {
            // given
            String guestToken = "guest-token-123";
            String keyword = "";

            Review review1 = reviewService.createReview(getReviewRequest(List.of(seat1, seat2), 5, "test-1"), user1.getId());
            Review review2 = reviewService.createReview(getReviewRequest(List.of(seat1), 4, "test-2"), user1.getId());
            Review review3 = reviewService.createReview(getReviewRequest(List.of(seat2), 3, "test-3"), user1.getId());

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
        void filterByAuditoriumId_Success() throws IOException {
            // given
            String guestToken = "guest-token-123";
            String keyword = "test";

            // seat1, seat2 는 auditorium1 소속
            Review review1 = reviewService.createReview(getReviewRequest(List.of(seat1, seat2), 5, "test-1"), user1.getId());
            Review review2 = reviewService.createReview(getReviewRequest(List.of(seat1), 4, "test-2"), user1.getId());

            // seat3 는 auditorium2 소속 (다른 상영관)
            Review review3 = reviewService.createReview(getReviewRequest(List.of(seat3), 3, "test-3"), user1.getId());

            ReviewSearchCondition condition = ReviewSearchCondition.builder()
                    .keyword(keyword)
                    .auditoriumId(auditorium1.getId()) // auditorium1 으로 필터링
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

            // review3 는 다른 상영관 리뷰이므로 포함되지 않아야 한다.
            // 따라서 응답 리스트의 크기는 review1, review2 만큼 2개여야 한다.
            Assertions.assertThat(responses).hasSize(2);

            // 포함된 리뷰가 filter 조건에 맞는지 확인 (예: auditorium1 소속 좌석 리뷰)
            Assertions.assertThat(responses).extracting("content")
                    .containsExactlyInAnyOrder(review1.getContent(), review2.getContent());

            // 혹은 첫번째 리뷰 내용이 review1 content인지 체크
            Assertions.assertThat(responses.get(0).content()).isIn(review1.getContent(), review2.getContent());
        }


        @Test
        @DisplayName("상영관 필터시 존재하지 않는 auditoriumId 입력")
        void filterByAuditoriumId_NotExist() throws IOException {
            // given
            String guestToken = "guest-token-123";
            String keyword = "test";

            /// seat1, seat2 는 auditorium1 소속
            Review review1 = reviewService.createReview(getReviewRequest(List.of(seat1, seat2), 5, "test-1"), user1.getId());
            Review review2 = reviewService.createReview(getReviewRequest(List.of(seat1), 4, "test-2"), user1.getId());

            /// seat3 는 auditorium2 소속 (다른 상영관)
            Review review3 = reviewService.createReview(getReviewRequest(List.of(seat3), 3, "test-3"), user1.getId());

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
        void filterByHashTags_Success() throws IOException {
            // given
            String guestToken = "guest-token-123";
            String keyword = "test";

            List<Long> tag1Ids = List.of(hashTag1.getId(), hashTag2.getId(), hashTag3.getId());
            List<Long> tag2Ids = List.of(hashTag4.getId(), hashTag5.getId(), hashTag6.getId());


            Review review1 = reviewService.createReview(getReviewRequest(List.of(seat1, seat2), 5, tag1Ids), user1.getId());
            Review review2 = reviewService.createReview(getReviewRequest(List.of(seat1), 4, tag2Ids), user1.getId());


            ReviewSearchCondition condition = ReviewSearchCondition.builder()
                    .keyword(keyword)
                    .sort(SortType.LATEST)
                    .hashTagIds(List.of(hashTag1.getId()))
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
        void filterByHashTags_NotExist() throws IOException {
            // given
            String guestToken = "guest-token-123";
            String keyword = "test";

            List<Long> tag1Ids = List.of(hashTag1.getId(), hashTag2.getId(), hashTag3.getId());
            List<Long> tag2Ids = List.of(hashTag4.getId(), hashTag5.getId(), hashTag6.getId());

            Review review1 = reviewService.createReview(getReviewRequest(List.of(seat1, seat2), 5, tag1Ids), user1.getId());
            Review review2 = reviewService.createReview(getReviewRequest(List.of(seat1), 4, tag2Ids), user1.getId());

            ReviewSearchCondition condition = ReviewSearchCondition.builder()
                    .keyword(keyword)
                    .sort(SortType.LATEST)
                    .hashTagIds(List.of(9999L))
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

    private ReviewRequest getReviewRequest(List<Seat> seats, double rating, List<Long> hashTagIds) {

        /// 좌석 번호
        List<String> seatIds = seats.stream()
                .map(Seat::getId)
                .toList();

        return ReviewRequest.builder()
                .seatIds(seatIds)
                .title("review title")
                .content("content")
                .movieTitle("ReviewTestTitle1")
                .imageUrls(null)
                .rating(rating)
                .hashtags(hashTagIds)
                .build();
    }

}
