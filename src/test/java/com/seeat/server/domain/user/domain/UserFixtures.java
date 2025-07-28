package com.seeat.server.domain.user.domain;

import com.seeat.server.domain.theater.domain.entity.MovieGenre;
import com.seeat.server.domain.user.domain.entity.User;
import com.seeat.server.domain.user.domain.entity.UserGrade;
import com.seeat.server.domain.user.domain.entity.UserSocial;

import java.util.*;

/**
 * [ 유저 도메인을 생성해두는 Fixtures ]입니다.
 * - 테스트에 사용할 도메인들을 미리 정의해두어, 테스트의 가독성을 높여주도록 수행합니다.
 */

public class UserFixtures {

    public static User createUser() {
        return User.builder()
                .email("test@test.com")
                .socialId("test" + UUID.randomUUID())
                .nickname("testNickname")
                .grade(UserGrade.PLATINUM)
                .imageUrl("www.test.imageUrl")
                .social(UserSocial.KAKAO)
                .genres(List.of(MovieGenre.ACTION, MovieGenre.ROMANCE, MovieGenre.DRAMA))
                .build();
    }

    public static User fakeUser() {
        return User.builder()
                .id(999L)
                .email("test@test.com")
                .socialId("testSocialId")
                .nickname("testNickname")
                .grade(UserGrade.PLATINUM)
                .imageUrl("www.test.imageUrl")
                .social(UserSocial.KAKAO)
                .genres(List.of(MovieGenre.ACTION, MovieGenre.ROMANCE, MovieGenre.DRAMA))
                .build();
    }

}
