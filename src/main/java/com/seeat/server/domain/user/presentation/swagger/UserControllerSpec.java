package com.seeat.server.domain.user.presentation.swagger;

import com.seeat.server.domain.user.application.dto.UserSignUpRequest;
import com.seeat.server.global.response.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;

public interface UserControllerSpec {

    /**
     * 회원가입 API
     * @param request
     * @param tempUserKey
     * @return
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

    @PostMapping("/logout")
    @Operation(
            summary = "로그아웃",
            description = "refreshToken 삭제하여 로그아웃합니다."
    )
    ApiResponse<Void> userLogout(HttpServletRequest request, HttpServletResponse response);

    @PostMapping("/dev/long-token")
    @Operation(summary = "개발용 토큰 생성",
            description = "30일 동안 유효한 개발용 토큰을 생성합니다"
    )
    ApiResponse<Void> generateDevToken(Authentication authentication, HttpServletResponse response);
}
