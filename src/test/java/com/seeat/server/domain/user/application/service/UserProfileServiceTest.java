package com.seeat.server.domain.user.application.service;

import com.seeat.server.domain.seat.domain.AuditoriumFixtures;
import com.seeat.server.domain.seat.domain.TheaterFixtures;
import com.seeat.server.domain.theater.application.dto.response.AuditoriumResponse;
import com.seeat.server.domain.theater.domain.entity.Auditorium;
import com.seeat.server.domain.theater.domain.entity.MovieGenre;
import com.seeat.server.domain.theater.domain.entity.Theater;
import com.seeat.server.domain.theater.domain.repository.AuditoriumRepository;
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
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

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

    @Nested
    @DisplayName("사용자 정보 조회 테스트")
    class getUserInfo{
        @Test
        @DisplayName("로그인한 유저 정보 정상 조회")
        void getUserInfo_Success() {
            // given
            User user = UserFixtures.createUser();
            Theater theater = TheaterFixtures.createTheater();
            repository.save(user);
            theaterRepository.save(theater);
            Auditorium auditorium = AuditoriumFixtures.createAuditorium(theater, "aud1");
            auditoriumRepository.save(auditorium);
            userAuditoriumRepository.save(UserAuditorium.of(user, auditorium));


            // when
            UserInfoResponse response = sut.getUserInfo(user.getId());


            // then
            assertEquals(user.getId(), response.id());
            assertEquals(user.getEmail(), response.email());
            assertEquals(user.getSocialId(), response.socialId());
            assertEquals(user.getUsername(), response.username());
            assertEquals(user.getImageUrl(), response.imageUrl());
            assertEquals(user.getNickname(), response.nickname());
            assertEquals(user.getGenres(), response.genres());
            assertEquals(user.getSocial(), response.social());
            List<AuditoriumResponse> auditoriumResponse = List.of(AuditoriumResponse.from(auditorium));
            assertEquals(auditoriumResponse, response.auditoriums());
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
        void updateUserInfo_Success() {
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
            String newImageUrl = "https://update.image.url";
            List<MovieGenre> newGenres = List.of(MovieGenre.COMEDY, MovieGenre.HORROR);
            List<String> auditoriumIds = List.of("audTest2");
            UserInfoUpdateRequest request = new UserInfoUpdateRequest(newNickname, newImageUrl, newGenres, auditoriumIds);

            // when
            UserInfoUpdateResponse response = sut.updateUserInfo(user.getId(), request);

            // then
            assertEquals(newNickname, response.nickname());
            assertEquals(newImageUrl, response.imageUrl());
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
            String newImageUrl = "https://update.image.url";
            List<MovieGenre> newGenres = List.of(MovieGenre.COMEDY, MovieGenre.HORROR);
            List<String> auditoriumIds = List.of("aud1");
            UserInfoUpdateRequest request = new UserInfoUpdateRequest(newNickname, newImageUrl, newGenres, auditoriumIds);

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
            String newImageUrl = "https://update.image.url";
            List<MovieGenre> newGenres = List.of(MovieGenre.COMEDY, MovieGenre.HORROR);
            List<String> auditoriumIds = List.of("aud1");
            UserInfoUpdateRequest request = new UserInfoUpdateRequest(newNickname, newImageUrl, newGenres, auditoriumIds);

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

}
