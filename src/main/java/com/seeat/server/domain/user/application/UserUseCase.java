package com.seeat.server.domain.user.application;

import com.seeat.server.domain.theater.domain.entity.Auditorium;
import com.seeat.server.domain.theater.domain.entity.MovieGenre;
import com.seeat.server.domain.user.application.dto.request.UserSignUpRequest;
import com.seeat.server.domain.user.application.dto.response.UserGradeResponse;
import com.seeat.server.domain.user.application.dto.response.UserInfoResponse;
import com.seeat.server.domain.user.application.dto.response.UserInfoUpdateResponse;
import com.seeat.server.domain.user.domain.entity.User;
import com.seeat.server.domain.user.domain.entity.UserSocial;
import com.seeat.server.global.response.ApiResponse;
import com.seeat.server.security.oauth2.application.dto.TempUserInfo;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.util.List;
import java.util.Optional;

public interface UserUseCase {

    // 이메일 중복 확인
    Optional<User> getUserByEmail(String email);

    // 소셜ID, 소셜로 유저 가입유무확인
    Optional<User> getUserBySocialAndSocialId(UserSocial social, String socialId);

    // 사용자 생성
    void createUser(TempUserInfo tempUserInfo, UserSignUpRequest request);

    // redis, cookie - refreshToken 삭제 (로그아웃)
    void logout(HttpServletRequest request, HttpServletResponse response);

    // 사용자 정보 조회
    UserInfoResponse getUserInfo(Long userId);

    // 사용자 정보 수정
    UserInfoUpdateResponse updateUserInfo(Long userId, String nickName, String imageUrl,
                                          List<MovieGenre> genres, List<Auditorium> auditoriums);

    // 등급 목록 조회
    List<UserGradeResponse> getUserGradeList();

    /// 외부 의존성을 위한 함수
    User getUser(Long userId);
}
