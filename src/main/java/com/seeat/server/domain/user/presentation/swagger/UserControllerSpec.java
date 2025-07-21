package com.seeat.server.domain.user.presentation.swagger;

import com.seeat.server.domain.user.application.dto.request.UserSignUpRequest;
import com.seeat.server.global.response.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;

@Tag(name = "유저 API", description = "회원을 관리하는 API 입니다.")
public interface UserControllerSpec {

    /**
     * 회원가입 API
     *
     * @param request     추가 정보 요청값
     * @param tempUserKey 임시유저정보
     * @return 회원가입 완료 응답
     */
    @PostMapping
    @Operation(
            summary = "회원가입",
            description = "최초 로그인 추가 회원가입입니다."
    )
    ApiResponse<Void> userSignUp(
            @RequestBody UserSignUpRequest request,
            @RequestHeader String tempUserKey
    );

    /**
     * 로그아웃 API
     *
     * @param request  HttpServletRequest 객체
     * @param response HttpServletResponse 객체
     * @return 로그아웃 완료 응답
     */
    @PostMapping("/logout")
    @Operation(
            summary = "로그아웃",
            description = "refreshToken 삭제하여 로그아웃합니다."
    )
    ApiResponse<Void> userLogout(HttpServletRequest request, HttpServletResponse response);

    /**
     * 개발용 토큰 발급 API
     *
     * @param response HTTP 응답 객체
     * @return 응답 메시지 (토큰 발급 완료 안내)
     */
    @PostMapping("/dev/long-token")
    @Operation(
            summary = "개발용 토큰 생성",
            description = "30일 동안 유효한 개발용 토큰을 생성합니다."
    )
    ApiResponse<Void> generateDevToken(HttpServletResponse response);

}
