package com.seeat.server.domain.theater.application.service;

import com.seeat.server.domain.review.application.dto.request.ReviewRequest;
import com.seeat.server.domain.review.application.usecase.ReviewUseCase;
import com.seeat.server.domain.review.domain.HashTagFixtures;
import com.seeat.server.domain.hashtag.domain.entity.HashTag;
import com.seeat.server.domain.hashtag.domain.entity.HashTagType;
import com.seeat.server.domain.review.domain.entity.Review;
import com.seeat.server.domain.hashtag.domain.repository.HashTagRepository;
import com.seeat.server.domain.theater.application.dto.response.AuditoriumDetailResponse;
import com.seeat.server.domain.theater.application.dto.response.SeatListResponse;
import com.seeat.server.domain.theater.application.dto.response.TheaterListResponse;
import com.seeat.server.domain.theater.domain.AuditoriumFixtures;
import com.seeat.server.domain.theater.domain.SeatFixtures;
import com.seeat.server.domain.theater.domain.TheaterFixtures;
import com.seeat.server.domain.theater.domain.entity.Auditorium;
import com.seeat.server.domain.theater.domain.entity.AuditoriumType;
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
import com.seeat.server.global.response.pageable.SliceResponse;
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
import java.util.NoSuchElementException;

import static org.assertj.core.api.Assertions.*;

@SpringBootTest
@Transactional
@ActiveProfiles("test")
class TheaterServiceIntTest {

    @Autowired
    private TheaterService sut;

    /// 값 저장을 위한 의존성
    @Autowired
    private ReviewUseCase reviewUseCase;

    @Autowired
    private AuditoriumRepository auditoriumRepository;

    @Autowired
    private TheaterRepository theaterRepository;

    @Autowired
    private SeatRepository seatRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private HashTagRepository hashTagRepository;

    /// 값
    private Seat seat1;
    private Seat seat2;
    private Theater theater;
    private Auditorium auditorium1;
    private Auditorium auditorium2;
    private Review review1;
    private User user1;
    private HashTag hashTag1;
    private HashTag hashTag2;
    private HashTag hashTag3;


    @BeforeEach
    void setUp() {
        theater = theaterRepository.save(TheaterFixtures.createTheater());
        auditorium1 = auditoriumRepository.save(AuditoriumFixtures.createAuditorium(theater, AuditoriumType.DOLBY));
        auditorium2 = auditoriumRepository.save(AuditoriumFixtures.createAuditorium(theater, AuditoriumType.IMAX));
        seat1 = seatRepository.save(SeatFixtures.createSeat(auditorium1));
        seat2 = seatRepository.save(SeatFixtures.createSeat2(auditorium1));
        user1 = userRepository.save(UserFixtures.createUser());
        hashTag1 = hashTagRepository.save(HashTagFixtures.createHashTag(HashTagType.SOUND, "음향이 좋아요"));
        hashTag2 = hashTagRepository.save(HashTagFixtures.createHashTag(HashTagType.COMPANION, "혼자 관람했어요"));
        hashTag3 = hashTagRepository.save(HashTagFixtures.createHashTag(HashTagType.ENVIRONMENT, "좌석이 넓어요"));

    }

    @Nested
    @DisplayName("상영관 조회")
    class Auditoriums {

        @Test
        @DisplayName("[happy] 영화관 타입으로 상영관 존재하는 영화관 목록 조회 성공")
        void loadTheatersByType_success() {
            // given
            AuditoriumType type = AuditoriumType.DOLBY;
            PageRequest pageRequest = getPageRequest();

            // when
            SliceResponse<TheaterListResponse> response = sut.loadTheatersByType(type, pageRequest);

            // then
            assertThat(response).isNotNull();
            assertThat(response.content()).isNotEmpty();
            assertThat(response.content().get(0).auditoriumId()).isEqualTo(auditorium1.getId());

        }



        @Test
        @DisplayName("[happy] 상영관 ID로 상세 정보 조회 성공")
        void loadAuditorium_success() {
            // given

            // when
            AuditoriumDetailResponse response = sut.loadAuditorium(auditorium1.getId());

            // then
            assertThat(response).isNotNull();
            assertThat(response.auditoriumId()).isEqualTo(auditorium1.getId());
            assertThat(response.auditoriumName()).isEqualTo(auditorium1.getName());
        }

        @Test
        @DisplayName("[happy] 상영관 ID로 리뷰 관련 상세 정보 조회 성공")
        void loadAuditorium_reviews_success() throws IOException {

            // given
            /// 리뷰 저장
            var request = ReviewRequest.builder()
                    .seatIds(List.of(seat1.getId()))
                    .content("test")
                    .movieTitle("ReviewTestTitle")
                    .imageUrls(null)
                    .rating(1.5)
                    .hashtags(List.of(hashTag1.getId(), hashTag2.getId(), hashTag3.getId()))
                    .build();

            reviewUseCase.createReview(request, user1.getId());

            // when
            AuditoriumDetailResponse response = sut.loadAuditorium(seat1.getAuditorium().getId());

            // then
            assertThat(response).isNotNull();
            assertThat(response.auditoriumId()).isEqualTo(seat1.getAuditorium().getId());
            assertThat(response.averageReview()).isEqualTo(1.5f);
            assertThat(response.reviewCount()).isEqualTo(1);

        }

        @Test
        @DisplayName("[unhappy] 존재하지 않는 상영관 ID로 상세 조회 시 예외 발생")
        void loadAuditorium_notFound_throwException() {
            // given
            String invalidId = "not-exist-id";

            // when & then
            assertThatThrownBy(() -> sut.loadAuditorium(invalidId))
                    .isInstanceOf(NoSuchElementException.class)
                    .hasMessageContaining(ErrorCode.NOT_AUDITORIUM.getMessage());
        }

    }

    @Nested
    @DisplayName("좌석 조회")
    class Seats {

        @Test
        @DisplayName("[happy] 상영관 ID로 좌석 리스트 조회 성공 및 정렬 확인")
        void loadSeatsByAuditorium_success() {

            // given

            // when
            List<SeatListResponse> seats = sut.loadSeatsByAuditorium(seat1.getAuditorium().getId());

            // then
            assertThat(seats).isNotEmpty();

            // 좌석이 행(row) 기준 오름차순, 열(column) 오름차순으로 정렬됐는지 확인
            for (int i = 1; i < seats.size(); i++) {
                SeatListResponse prev = seats.get(i - 1);
                SeatListResponse curr = seats.get(i);

                if (prev.row().equals(curr.row())) {
                    assertThat(prev.column()).isLessThanOrEqualTo(curr.column());
                } else {
                    assertThat(prev.row()).isLessThan(curr.row());
                }
            }
        }

        @Test
        @DisplayName("[unhappy] 존재하지 않는 상영관 ID로 좌석 조회 시 예외 발생")
        void loadSeatsByAuditorium_notFound_throwException() {
            // given
            String invalidId = "invalid-auditorium";

            // when & then
            assertThatThrownBy(() -> sut.loadSeatsByAuditorium(invalidId))
                    .isInstanceOf(NoSuchElementException.class)
                    .hasMessageContaining(ErrorCode.NOT_AUDITORIUM.getMessage());
        }

        @Test
        @DisplayName("[happy] 좌석 ID로 좌석 조회 성공")
        void getSeat_success() {
            // given
            // when
            Seat result = sut.getSeat(seat1.getId());

            // then
            assertThat(result).isNotNull();
            assertThat(result.getId()).isEqualTo(seat1.getId());
            assertThat(result.getRow()).isEqualTo(seat1.getRow());
            assertThat(result.getColumn()).isEqualTo(seat1.getColumn());
        }

        @Test
        @DisplayName("[unhappy] 존재하지 않는 좌석 ID 조회 시 예외 발생")
        void getSeat_notFound_throwException() {
            // given
            String invalidSeatId = "invalid-seat";

            // when & then
            assertThatThrownBy(() -> sut.getSeat(invalidSeatId))
                    .isInstanceOf(NoSuchElementException.class)
                    .hasMessageContaining(ErrorCode.NOT_SEAT.getMessage());

        }

    }


    private static PageRequest getPageRequest() {
        PageRequest pageRequest = PageRequest.builder().page(1).size(10).build();
        return pageRequest;
    }
}
