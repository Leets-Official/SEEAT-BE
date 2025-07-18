package com.seeat.server.domain.user.application.service;

import com.seeat.server.domain.theater.domain.entity.Auditorium;
import com.seeat.server.domain.theater.domain.entity.MovieGenre;
import com.seeat.server.domain.user.application.UserService;
import com.seeat.server.domain.user.application.dto.response.UserGradeResponse;
import com.seeat.server.domain.user.application.dto.response.UserInfoResponse;
import com.seeat.server.domain.user.application.dto.response.UserInfoUpdateResponse;
import com.seeat.server.domain.user.domain.UserFixtures;
import com.seeat.server.domain.user.domain.entity.User;
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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertIterableEquals;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
public class UserServiceTest {

    @Autowired
    private UserService sut;

    @Autowired
    private UserRepository repository;

    @Nested
    @DisplayName("사용자 정보 조회 테스트")
    class getUserInfo{
        @Test
        @DisplayName("로그인한 유저 정보 정상 조회")
        void getUserInfo_Success() {
            // given
            User user = UserFixtures.createUser();
            repository.save(user);

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
    }

    @Nested
    @DisplayName("사용자 정보 수정 테스트")
    class updateUserInfo {
        @Test
        @DisplayName("로그인한 유저 사용 정상 수정")
        void updateUserInfo_Success() {
            // given
            User user = UserFixtures.createUser();
            repository.save(user);

            // 수정 정보
            String newNickname = "updateNick";
            String newImageUrl = "https://update.image.url";
            List<MovieGenre> newGenres = List.of(MovieGenre.COMEDY, MovieGenre.HORROR);
            List<Auditorium> auditoriums = null;

            // when
            UserInfoUpdateResponse response = sut.updateUserInfo(user.getId(), newNickname, newImageUrl, newGenres, auditoriums);

            // then
            assertEquals(newNickname, response.nickname());
            assertEquals(newImageUrl, response.imageUrl());
            assertIterableEquals(newGenres, response.genres());
        }

        @Test
        @DisplayName("로그인한 유저 사용 수정 사용자 예외 발생")
        void updateUserInfo_Fail() {
            // given
            User user = UserFixtures.fakeUser();

            // 수정 정보
            String newNickname = "updateNick";
            String newImageUrl = "https://update.image.url";
            List<MovieGenre> newGenres = List.of(MovieGenre.COMEDY, MovieGenre.HORROR);
            List<Auditorium> auditoriums = null;

            // when & then
            Assertions.assertThatThrownBy(() -> sut.updateUserInfo(user.getId(), newNickname, newImageUrl, newGenres, auditoriums))
                    .isInstanceOf(NoSuchElementException.class)
                    .hasMessageContaining(ErrorCode.NOT_USER.getMessage());
        }
    }
}
