package com.seeat.server.domain.user.presentation;


import com.seeat.server.domain.user.application.dto.response.UserNicknameResponse;
import com.seeat.server.domain.user.application.usecase.UserUseCase;
import com.seeat.server.domain.user.application.dto.request.UserSignUpRequest;
import com.seeat.server.domain.user.domain.entity.User;
import com.seeat.server.domain.user.domain.entity.UserRole;
import com.seeat.server.domain.user.presentation.swagger.UserControllerSpec;
import com.seeat.server.global.response.ApiResponse;
import com.seeat.server.global.response.CustomException;
import com.seeat.server.global.response.ErrorCode;
import com.seeat.server.global.service.RedisService;
import com.seeat.server.security.jwt.service.TokenService;
import com.seeat.server.security.oauth2.application.dto.TempUserInfo;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/users")
public class UserController implements UserControllerSpec {

    private final UserUseCase userService;

    /**
     * 최초 로그인시 추가 회원가입을 진행합니다.
     *
     * @param request 추가 정보 요청값 (닉네임, 유저프로필, 선호 장르, 선호 상영관)
     * @param tempUserKey 임시유저정보 담긴 RedisKey
     * @return 회원가입 완료 응답
     */
    @PostMapping()
    public ApiResponse<Void> userSignUp(
            HttpServletResponse response,
            @RequestBody @Valid UserSignUpRequest request,
            @RequestHeader("Temp-User-Key") String tempUserKey) throws IOException {

        /// 헤더 값 없다면 예외 처리
        if (tempUserKey == null) {

            throw new CustomException(ErrorCode.NOT_TEMP_USER, null);
        }

        /// 서비스 로직 구현
        userService.createUser(tempUserKey, request, response);

        return ApiResponse.created();
    }

    /**
     * 중복 여부 판단 로직
     * @param nickname 사용할 닉네임
     */
    @GetMapping
    public ApiResponse<UserNicknameResponse> userDuplicateNickname(@RequestParam String nickname){

        /// 서비스 호출
        UserNicknameResponse response = userService.isNicknameDuplicated(nickname);

        return ApiResponse.ok(response);
    }


    /**
     * 로그아웃시 refreshToekn 쿠키, redis 삭제
     *
     * @param request  HttpServletRequest 객체
     * @param response HttpServletResponse 객체
     * @return 로그아웃 완료 응답
     */
    @PostMapping("/logout")
    public ApiResponse<Void> userLogout(
            HttpServletRequest request,
            HttpServletResponse response) {

        /// 서비스 호출
        userService.logout(request, response);

        /// 시큐리티 제거
        SecurityContextHolder.clearContext();

        return ApiResponse.ok(null);
    }

    /**
     * 개발 환경에서 사용할 수 있는 30일 유효 토큰을 생성합니다.
     *
     * @param response HTTP 응답 객체
     * @return 응답 메시지 (토큰 발급 완료 안내)
     */
    @PostMapping("/dev/long-token")
    public ApiResponse<Void> generateDevToken(
            HttpServletResponse response) {

        /// 서비스 호출
        userService.generateDev(response);

        return ApiResponse.created();
    }

}
