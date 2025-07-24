package com.seeat.server.domain.best.application.service;

import com.seeat.server.domain.best.application.dto.response.BestAuditoriumListResponse;
import com.seeat.server.domain.best.application.dto.response.BestReviewListResponse;
import com.seeat.server.domain.review.application.dto.request.ReviewRequest;
import com.seeat.server.domain.review.application.usecase.ReviewLikeUseCase;
import com.seeat.server.domain.review.application.usecase.ReviewUseCase;
import com.seeat.server.domain.review.domain.HashTagFixtures;
import com.seeat.server.domain.review.domain.entity.HashTag;
import com.seeat.server.domain.review.domain.entity.HashTagType;
import com.seeat.server.domain.review.domain.entity.Review;
import com.seeat.server.domain.review.domain.repository.HashTagRepository;
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
import com.seeat.server.global.response.pageable.PageRequest;
import com.seeat.server.global.response.pageable.SliceResponse;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static com.seeat.server.global.util.RedisKeyUtil.BEST_REVIEW_LIST_KEY;
import static com.seeat.server.global.util.RedisKeyUtil.BEST_AUDITORIUM_LIST_KEY;
import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@Transactional
@ActiveProfiles("test")
class BestContentServiceIntTest {

    @Autowired
    private BestContentService sut;

    @Autowired
    private RedisTemplate<String, String> redisTemplate;

    /// 좋아요 의존성
    @Autowired
    private ReviewLikeUseCase likeService;

    /// 저장을 위한 셋팅
    @Autowired
    private ReviewUseCase reviewService;

    /// 미리 저장되어있어야하는 의존성 추가
    @Autowired
    private HashTagRepository hashTagRepository;

    @Autowired
    private AuditoriumRepository auditoriumRepository;

    @Autowired
    private TheaterRepository theaterRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private SeatRepository seatRepository;

    /// 기본 데이터 등록
    private User user1;
    private User user2;
    private Theater theater;
    private Auditorium auditorium1;
    private Auditorium auditorium2;
    private Auditorium auditorium3;
    private Auditorium auditorium4;
    private Seat seat1;
    private Seat seat2;
    private HashTag hashTag1;
    private HashTag hashTag2;
    private HashTag hashTag3;


    /// 연관관계 미리 정의
    @BeforeEach
    void setUp() {
        theater = theaterRepository.save(TheaterFixtures.createTheater());
        auditorium1 = auditoriumRepository.save(AuditoriumFixtures.createAuditorium(theater));
        auditorium2 = auditoriumRepository.save(AuditoriumFixtures.createAuditorium(theater));
        auditorium3 = auditoriumRepository.save(AuditoriumFixtures.createAuditorium(theater));
        auditorium4 = auditoriumRepository.save(AuditoriumFixtures.createAuditorium(theater));
        seat1 = seatRepository.save(SeatFixtures.createSeat(auditorium1));
        seat2 = seatRepository.save(SeatFixtures.createSeat2(auditorium2));
        user1 = userRepository.save(UserFixtures.createUser());
        user2 = userRepository.save(UserFixtures.createUser());
        hashTag1 = hashTagRepository.save(HashTagFixtures.createHashTag(HashTagType.SOUND, "음향이 좋아요"));
        hashTag2 = hashTagRepository.save(HashTagFixtures.createHashTag(HashTagType.COMPANION, "혼자 관람했어요"));
        hashTag3 = hashTagRepository.save(HashTagFixtures.createHashTag(HashTagType.ENVIRONMENT, "좌석이 넓어요"));
    }

    @AfterEach()
    void tearDown() {
        redisTemplate.delete(BEST_REVIEW_LIST_KEY);
        redisTemplate.delete(BEST_AUDITORIUM_LIST_KEY);
    }

    @Nested
    @DisplayName("인기 리뷰 조회")
    class FindBestReviewContentTest {

        @Test
        @DisplayName("[happy] 인기있는 리뷰 조회 및 requestSize(최신순) 조회")
        public void load_best_reviews() throws Exception {

            //given

            /// 좋아요가 0개인 리뷰들를 저장한다.
            for (int i = 0; i < 5; i++) {
                var request = getReviewRequest(5);
                reviewService.createReview(request, user2.getId());
            }

            /// 리뷰 저장 후
            var request = getReviewRequest(3);
            Review review = reviewService.createReview(request, user1.getId());

            /// 유저1이 작성한 리뷰에만 좋아요 구현
            likeService.reviewLike(user2.getId(), review.getId());

            /// 페이징 처리
            PageRequest pageRequest = PageRequest.builder()
                    .page(1)
                    .size(4)
                    .build();
            /// 저장
            sut.saveBestContents();

            //when
            SliceResponse<BestReviewListResponse> response = sut.loadBestReviews(pageRequest);

            //then
            assertNotNull(response);
            Assertions.assertEquals(response.content().get(0).reviewId(), review.getId());
            Assertions.assertEquals(response.content().size(), 4);
            Assertions.assertTrue(
                    response.content().get(2).createdAt().isAfter(response.content().get(1).createdAt())
            );
        }


        @Test
        @DisplayName("[happy] 좋아요가 하나도 없을 때에도 N개가 조회됨")
        public void load_random_reviews() throws Exception {

            //given

            /// 5개의 리뷰를 저장한다.
            for (int i = 0; i < 5; i++) {
                var request = getReviewRequest(3.2);
                reviewService.createReview(request, user2.getId());
            }

            PageRequest pageRequest = PageRequest.builder()
                    .page(1)
                    .size(4)
                    .build();
            /// 저장
            sut.saveBestContents();

            // when

            SliceResponse<BestReviewListResponse> response = sut.loadBestReviews(pageRequest);

            //then
            assertNotNull(response);
            Assertions.assertEquals(response.content().size(), 4);
        }

    }


    @Nested
    @DisplayName("인기 상영관 조회")
    class FindBestAuditoriumContentTest {

        @Test
        @DisplayName("[happy] 인기있는 상영관 조회 및 requestSize로 조회")
        public void load_best_auditoriums() throws Exception {

            //given
            /// 2번째 좌석에 3개의 리뷰를 저장한다.
            for (int i = 0; i < 4; i++) {
                var request = getReviewRequest(1, seat2.getId());
                reviewService.createReview(request, user2.getId());
            }

            /// 1번 좌석에 5점짜리 리뷰 저장
            var request = getReviewRequest(5);
            reviewService.createReview(request, user1.getId());

            /// DB에 저장하는 로직
            sut.saveBestContents();

            /// 페이징 처리
            PageRequest pageRequest = PageRequest.builder()
                    .page(1)
                    .size(4)
                    .build();

            // when
            SliceResponse<BestAuditoriumListResponse> response = sut.loadBestTheaters(pageRequest);

            //then
            assertNotNull(response);
            Assertions.assertEquals(response.content().size(), 4);
            Assertions.assertEquals(response.content().get(0).auditoriumId(), seat1.getAuditorium().getId());
            Assertions.assertEquals(response.content().get(1).auditoriumId(), seat2.getAuditorium().getId());
        }

        @Test
        @DisplayName("[happy] 좋아요가 하나도 없을 때에도 N개가 조회됨")
        public void load_random_auditoriums() throws Exception {

            //given
            PageRequest pageRequest = PageRequest.builder()
                    .page(1)
                    .size(4)
                    .build();

            /// 5개의 리뷰를 저장한다.
            for (int i = 0; i < 5; i++) {
                var request = getReviewRequest(4);
                reviewService.createReview(request, user2.getId());
            }

            /// DB에 저장하는 로직
            sut.saveBestContents();

            // when
            SliceResponse<BestAuditoriumListResponse> response = sut.loadBestTheaters(pageRequest);

            //then
            assertNotNull(response);
            Assertions.assertEquals(response.content().size(), 4);
        }
    }

    @Nested
    @DisplayName("레디스 캐싱 생성 테스트")
    class SaveRedisCachingTest{

        @Test
        @DisplayName("[redis] 인기 리뷰 목록이 Redis에 저장되는지 확인")
        public void redis_contains_best_reviews_key() throws Exception {
            // given
            reviewService.createReview(getReviewRequest(5), user1.getId());
            sut.saveBestContents();

            // when
            Boolean hasKey = redisTemplate.hasKey(BEST_REVIEW_LIST_KEY);

            // then
            assertTrue(hasKey != null && hasKey);
        }

        @Test
        @DisplayName("[redis] 인기 상영관 목록이 Redis에 저장되는지 확인")
        public void redis_contains_best_auditoriums_key() throws Exception {
            // given
            reviewService.createReview(getReviewRequest(4), user1.getId());
            sut.saveBestContents();

            // when
            Boolean hasKey = redisTemplate.hasKey(BEST_AUDITORIUM_LIST_KEY);

            // then
            assertTrue(hasKey != null && hasKey);
        }



    }

    @Nested
    @DisplayName("레디스 캐싱 조회 테스트")
    class LoadRedisCachingTest {

        @Test
        @DisplayName("[happy] redis 인기 리뷰 목록에 값이 있으면 Redis에서 조회")
        public void load_redis_best_reviews() throws Exception {

            // given
            Review review = reviewService.createReview(getReviewRequest(5), user1.getId());
            sut.saveBestContents();

            // Redis에 key가 존재해야 함
            assertTrue(redisTemplate.hasKey(BEST_REVIEW_LIST_KEY));

            // when
            SliceResponse<BestReviewListResponse> response = sut.loadBestReviews(
                    PageRequest.builder().page(1).size(4).build());

            // then
            assertNotNull(response);
            assertFalse(response.content().isEmpty());
            assertEquals(review.getId(), response.content().get(0).reviewId());
        }


        @Test
        @DisplayName("[happy] redis 인기 리뷰 목록에 값이 없으면 DB에서 조회")
        public void load_db_best_reviews() throws Exception {

            // given
            Review review = reviewService.createReview(getReviewRequest(3), user1.getId());

            /// 1차 레디스 저장X 및 2차로 Redis에서 삭제 (강제로 캐시 없음 상태로 만들기)
            redisTemplate.delete(BEST_REVIEW_LIST_KEY);

            // when
            SliceResponse<BestReviewListResponse> response = sut.loadBestReviews(
                    PageRequest.builder().page(1).size(4).build());

            // then
            assertNotNull(response);
            assertFalse(response.content().isEmpty());
            assertEquals(review.getId(), response.content().get(0).reviewId());
        }

        @Test
        @DisplayName("[happy] redis 인기 상영관 목록에 값이 있으면 Redis에서 조회")
        public void load_redis_best_auditoriums() throws Exception {

            // given
            Review review = reviewService.createReview(getReviewRequest(5), user1.getId());
            sut.saveBestContents();

            // Redis에 key가 존재해야 함
            assertTrue(redisTemplate.hasKey(BEST_AUDITORIUM_LIST_KEY));

            // when
            SliceResponse<BestAuditoriumListResponse> response = sut.loadBestTheaters(
                    PageRequest.builder().page(1).size(4).build());

            // then
            assertNotNull(response);
            assertFalse(response.content().isEmpty());
            assertEquals(review.getSeat().getAuditorium().getId(), response.content().get(0).auditoriumId());
        }

        @Test
        @DisplayName("[happy] redis 인기 상영관 목록에 값이 없으면 DB에서 조회")
        public void load_db_best_auditoriums() throws Exception {

            // given
            Review review = reviewService.createReview(getReviewRequest(5), user1.getId());

            /// 1차 레디스 저장X 및 2차로 Redis에서 삭제 (강제로 캐시 없음 상태로 만들기)
            redisTemplate.delete(BEST_AUDITORIUM_LIST_KEY);

            // when
            SliceResponse<BestAuditoriumListResponse> response = sut.loadBestTheaters(
                    PageRequest.builder().page(1).size(4).build());

            // then
            assertNotNull(response);
            assertFalse(response.content().isEmpty());
            assertEquals(review.getSeat().getAuditorium().getId(), response.content().get(0).auditoriumId());

        }
    }
    @Nested
    @DisplayName("기존 데이터의 삭제 트랜잭션 테스트")
    class DeleteRedisCachingTest {

        @Test
        @DisplayName("[happy] 인기 리뷰를 작성한 사용자가 삭제할 경우, redis 인기 리뷰 목록에서 같이 사라지는지")
        public void delete_reviews_redis_best_reviews() throws Exception {

            //given

            //when

            //then
        }

        @Test
        @DisplayName("[happy] 일반 리뷰를 작성한 사용자가 삭제할 경우, redis 인기 리뷰 목록는 업데이트 되지 않는다.")
        public void delete_normal_reviews_redis_best_reviews() throws Exception {

            //given

            //when

            //then
        }

        @Test
        @DisplayName("[happy] 리뷰를 작성한 사용자가 삭제할 경우, redis 인기상영관의 후기 개수와 후기 평점이 변경되는지")
        public void delete_reviews_redis_best_auditoriums() throws Exception {

            //given

            //when

            //then
        }

    }


    /// 공통 함수
    private ReviewRequest getReviewRequest(double rating) {
        return ReviewRequest.builder()
                .seatId(seat1.getId())
                .content("test")
                .movieTitle("ReviewTestTitle")
                .photos(null)
                .rating(rating)
                .hashtags(List.of(hashTag1.getId(), hashTag2.getId(), hashTag3.getId()))
                .build();
    }

    private ReviewRequest getReviewRequest(double rating, String seatId) {

        return ReviewRequest.builder()
                .seatId(seatId)
                .content("test")
                .movieTitle("ReviewTestTitle")
                .photos(null)
                .rating(rating)
                .hashtags(List.of(hashTag1.getId(), hashTag2.getId(), hashTag3.getId()))
                .build();
    }

}
