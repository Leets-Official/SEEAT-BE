package com.seeat.server.domain.user.presentation;


import com.nimbusds.openid.connect.sdk.claims.UserInfo;
import com.seeat.server.domain.user.application.UserUseCase;
import com.seeat.server.domain.user.application.dto.request.UserInfoUpdateRequest;
import com.seeat.server.domain.user.application.dto.request.UserSignUpRequest;
import com.seeat.server.domain.user.application.dto.response.UserInfoResponse;
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
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/users")
public class UserController implements UserControllerSpec {

    private final RedisService redisService;
    private final UserUseCase userService;
    private final TokenService tokenService;

    /**
     * 최초 로그인시 추가 회원가입을 진행합니다.
     *
     * @param request     추가 정보 요청값 (닉네임, 유저프로필, 선호 장르, 선호 극장)
     * @param tempUserKey 임시유저정보 담긴 RedisKey
     * @return 회원가입 완료 응답
     */
    @PostMapping
    public ApiResponse<Void> userSignUp(
            @Valid @RequestBody UserSignUpRequest request,
            @RequestHeader("Temp-User-Key") String tempUserKey) {

        TempUserInfo tempUserInfo = redisService.getValues(tempUserKey, TempUserInfo.class);

        if (tempUserInfo == null) {

            throw new CustomException(ErrorCode.NOT_TEMP_USER, null);
        }
        userService.createUser(tempUserInfo, request);
        redisService.deleteValues(tempUserKey);

        return ApiResponse.created();
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

        userService.logout(request, response);
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

        tokenService.generateDevTokensAndSetHeaders(1L, "admin", UserRole.ADMIN, response);

        return ApiResponse.created();
    }

    /**
     * 마이페이지에서 사용자 정보를 조회 합니다.
     *
     * @param user Jwt 기반 SecurityContext 저장되어있는 유저
     * @return UserInfoResponse DTO 응답
     */
    @GetMapping
    public ApiResponse<UserInfoResponse> getUserInfo(
            @AuthenticationPrincipal User user){

        // 사용자 정보 조회
        UserInfoResponse response = userService.getUserInfo(user.getId());

        return ApiResponse.ok(response);
    }



}
