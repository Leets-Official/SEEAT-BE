package com.seeat.server.domain.user.application;

import com.seeat.server.domain.user.application.dto.request.UserSignUpRequest;
import com.seeat.server.domain.user.application.dto.response.UserGradeResponse;
import com.seeat.server.domain.user.application.dto.response.UserInfoResponse;
import com.seeat.server.domain.user.domain.entity.User;
import com.seeat.server.domain.user.domain.entity.UserSocial;
import com.seeat.server.global.response.ApiResponse;
import com.seeat.server.security.oauth2.application.dto.TempUserInfo;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

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

    // 사용자 등급 조회
    UserGradeResponse getUserGrade(Long userId);

    /// 외부 의존성을 위한 함수
    User getUser(Long userId);
}
