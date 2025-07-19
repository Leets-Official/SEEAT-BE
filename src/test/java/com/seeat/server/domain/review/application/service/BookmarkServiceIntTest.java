package com.seeat.server.domain.review.application.service;

import com.seeat.server.domain.review.application.dto.request.BookmarkRequest;
import com.seeat.server.domain.review.application.dto.response.ReviewListResponse;
import com.seeat.server.domain.review.application.usecase.ReviewLikeUseCase;
import com.seeat.server.domain.review.domain.entity.Bookmark;
import com.seeat.server.domain.review.domain.entity.Review;
import com.seeat.server.domain.review.domain.entity.ReviewLike;
import com.seeat.server.domain.review.domain.repository.BookmarkRepository;
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
import com.seeat.server.global.response.pageable.PageRequest;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.Slice;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.util.NoSuchElementException;

import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;

@SpringBootTest
@Transactional
@ActiveProfiles("test")
class BookmarkServiceIntTest {

    @Autowired
    private BookmarkService sut;

    @Autowired
    private BookmarkRepository bookmarkRepository;


    /// 준비해야되는 의존성
    @Autowired
    private ReviewRepository reviewRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private AuditoriumRepository auditoriumRepository;

    @Autowired
    private TheaterRepository theaterRepository;

    @Autowired
    private SeatRepository seatRepository;

    /// 기타 의존성
    @Autowired
    private ReviewLikeUseCase likeService;

    private Seat seat;
    private User user;
    private Theater theater;
    private Auditorium auditorium;

    @BeforeEach
    void setUp() {
        theater = theaterRepository.save(TheaterFixtures.createTheater());
        auditorium = auditoriumRepository.save(AuditoriumFixtures.createAuditorium(theater));
        seat = seatRepository.save(SeatFixtures.createSeat(auditorium));
        user = userRepository.save(UserFixtures.createUser());
    }

    /**
     * 리뷰를 직접 생성하여 저장하고, 필요한 경우 해시태그 연관관계도 세팅한다.
     */
    private Review saveReview(User user, Seat seat, String content, int rating) {
        Review review = Review.builder()
                .seat(seat)
                .user(user)
                .content(content)
                .movieTitle("ReviewTestTitle")
                .rating(rating)
                .build();
        review = reviewRepository.save(review);

        return review;
    }

    @Nested
    @DisplayName("생성 테스트")
    class SaveBookmark {

        @Test
        @DisplayName("[happy] 유저가 존재하는 리뷰를 바탕으로 북마크를 생성합니다")
        public void saveBookmark_user_happy() {
            //given
            Review review = saveReview(user, seat, "test", 5);

            var bookmarkRequest = BookmarkRequest.builder()
                    .userId(user.getId())
                    .reviewId(review.getId())
                    .build();

            //when
            Bookmark bookmark = sut.createBookmark(bookmarkRequest);

            //then
            Assertions.assertEquals(bookmark.getUser().getId(), user.getId());
            Assertions.assertEquals(bookmark.getReview().getId(), review.getId());
        }

        @Test
        @DisplayName("[unhappy] 리뷰가 없는 경우 예외 체크")
        public void createBookmark_review_throw_exception() {
            //given
            var bookmarkRequest = BookmarkRequest.builder()
                    .userId(user.getId())
                    .reviewId(9999L) // 존재하지 않는 리뷰 ID
                    .build();

            //when & then
            assertThatThrownBy(() -> sut.createBookmark(bookmarkRequest))
                    .isInstanceOf(NoSuchElementException.class)
                    .hasMessageContaining(ErrorCode.NOT_REVIEW.getMessage());
        }

        @Test
        @DisplayName("[unhappy] 유저가 없는 경우 예외 체크")
        public void createBookmark_user_throw_exception() {
            //given
            Review review = saveReview(user, seat, "test", 5);

            var bookmarkRequest = BookmarkRequest.builder()
                    .userId(9999L) // 존재하지 않는 유저
                    .reviewId(review.getId())
                    .build();

            //when & then
            assertThatThrownBy(() -> sut.createBookmark(bookmarkRequest))
                    .isInstanceOf(NoSuchElementException.class)
                    .hasMessageContaining(ErrorCode.NOT_USER.getMessage());
        }
    }

    @Nested
    @DisplayName("조회 테스트")
    class LoadBookmark {

        @Test
        @DisplayName("[happy] 추가한 북마크가 존재한다면, 정상적으로 목록 조회")
        public void loadBookmark_user_happy() {
            //given
            Review review = saveReview(user, seat, "test", 5);

            var bookmarkRequest = BookmarkRequest.builder()
                    .userId(user.getId())
                    .reviewId(review.getId())
                    .build();

            sut.createBookmark(bookmarkRequest);

            var pageRequest = PageRequest.builder().page(1).size(10).build();

            //when
            var response = sut.loadMyBookmarks(user.getId(), pageRequest);

            //then
            Assertions.assertEquals(1, response.getContent().size());
            Assertions.assertTrue(response.hasContent());
            Assertions.assertEquals(response.getContent().get(0).reviewId(), review.getId());
        }

        @Test
        @DisplayName("[happy] 북마크가 없다면, 정상적으로 빈 리스트 조회")
        public void loadBookmark_user_none_happy() {
            //when
            var pageRequest = PageRequest.builder().page(1).size(10).build();

            var response = sut.loadMyBookmarks(user.getId(), pageRequest);

            //then
            Assertions.assertTrue(response.getContent().isEmpty());
        }
    }

    @Nested
    @DisplayName("좋아요 반영 조회 테스트")
    class LoadReviewsByLike {

        @Test
        @DisplayName("[happy] 정상적으로 좋아요 개수가 출력")
        public void happyLoad_Like(){

            //given
            Review review = saveReview(user, seat, "test", 5);

            var bookmarkRequest = BookmarkRequest.builder()
                    .userId(user.getId())
                    .reviewId(review.getId())
                    .build();

            PageRequest pageRequest = PageRequest.builder().page(1).size(10).build();

            // 좋아요 추가
            likeService.reviewLike(user.getId(), review.getId());
            sut.createBookmark(bookmarkRequest);

            //when
            Slice<ReviewListResponse> responses = sut.loadMyBookmarks(user.getId(), pageRequest);

            //then
            Assertions.assertEquals(1, responses.getContent().size());
            Assertions.assertTrue(responses.hasContent());
            Assertions.assertEquals(responses.getContent().get(0).heartCount(), 1L);

        }
    }

    @Nested
    @DisplayName("삭제 테스트")
    class DeleteBookmark {

        @Test
        @DisplayName("[happy] 내가 추가한 북마크라면, 정상적으로 삭제")
        public void deleteBookmark_user_happy() {
            //given
            Review review = saveReview(user, seat, "delete test", 4);

            var bookmarkRequest = BookmarkRequest.builder()
                    .userId(user.getId())
                    .reviewId(review.getId())
                    .build();

            Bookmark bookmark = sut.createBookmark(bookmarkRequest);

            //when
            sut.deleteBookmark(bookmark.getId(), user.getId());

            //then
            Assertions.assertFalse(bookmarkRepository.findById(bookmark.getId()).isPresent());
        }

        @Test
        @DisplayName("[unhappy] 내가 추가하지 않은 북마크라면, 예외 발생")
        public void deleteBookmark_user_throw_exception() {
            //given
            User otherUser = userRepository.save(UserFixtures.createUser());

            Review review = saveReview(user, seat, "delete test", 4);

            Bookmark bookmark = sut.createBookmark(
                    BookmarkRequest.builder()
                            .userId(user.getId())
                            .reviewId(review.getId())
                            .build()
            );

            //when & then
            assertThatThrownBy(() -> sut.deleteBookmark(bookmark.getId(), otherUser.getId()))
                    .isInstanceOf(IllegalStateException.class)
                    .hasMessageContaining(ErrorCode.NOT_OWN_BOOKMARK.getMessage());
        }
    }
}
