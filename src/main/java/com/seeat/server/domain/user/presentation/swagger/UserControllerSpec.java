package com.seeat.server.domain.user.presentation.swagger;

import com.seeat.server.domain.theater.domain.entity.MovieGenre;
import com.seeat.server.domain.theater.domain.entity.Theater;
import com.seeat.server.domain.user.application.dto.request.UserSignUpRequest;
import com.seeat.server.domain.user.application.dto.response.UserGradeResponse;
import com.seeat.server.domain.user.application.dto.response.UserInfoResponse;
import com.seeat.server.domain.user.domain.entity.User;
import com.seeat.server.domain.user.domain.entity.UserGrade;
import com.seeat.server.domain.user.domain.entity.UserSocial;
import com.seeat.server.global.response.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;

import java.util.List;

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
    @Operation(
            summary = "개발용 토큰 생성",
            description = "30일 동안 유효한 개발용 토큰을 생성합니다."
    )
    ApiResponse<Void> generateDevToken(HttpServletResponse response);

    @GetMapping
    @Operation(summary = "사용자 정보 조회",
                description = "마이페이지에서 사용자 정보를 조회합니다.")
    ApiResponse<UserInfoResponse> getUserInfo(
            @AuthenticationPrincipal User user);

    @GetMapping("/me/grade")
    @Operation(summary = "사용자 등급 조회",
            description = "마이페이지에서 사용자 등급을 조회합니다.")
    ApiResponse<UserGradeResponse> getUserGrade(
            @AuthenticationPrincipal User user);
}
