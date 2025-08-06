package com.seeat.server.domain.user.application.usecase;

import com.seeat.server.domain.user.application.dto.request.UserSignUpRequest;
import com.seeat.server.domain.user.application.dto.response.UserNicknameResponse;
import com.seeat.server.domain.user.domain.entity.User;
import com.seeat.server.domain.user.domain.entity.UserSocial;
import com.seeat.server.security.oauth2.application.dto.TempUserInfo;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.util.Optional;

public interface UserUseCase {

    // 이메일 중복 확인
    Optional<User> getUserByEmail(String email);

    // 닉네임 중복 확인
    UserNicknameResponse isNicknameDuplicated(String nickname);

    // 소셜ID, 소셜로 유저 가입유무확인
    Optional<User> getUserBySocialAndSocialId(UserSocial social, String socialId);

    // 사용자 생성
    User createUser(TempUserInfo tempUserInfo, UserSignUpRequest request) throws IOException;

    // redis, cookie - refreshToken 삭제 (로그아웃)
    void logout(HttpServletRequest request, HttpServletResponse response);

    /// 외부 의존성을 위한 함수
    User getUser(Long userId);


}
