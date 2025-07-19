package com.seeat.server.domain.user.presentation.swagger;

import com.seeat.server.domain.user.application.dto.request.UserInfoUpdateRequest;
import com.seeat.server.domain.user.application.dto.request.UserSignUpRequest;
import com.seeat.server.domain.user.application.dto.response.UserGradeResponse;
import com.seeat.server.domain.user.application.dto.response.UserInfoResponse;
import com.seeat.server.domain.user.application.dto.response.UserInfoUpdateResponse;
import com.seeat.server.domain.user.domain.entity.User;
import com.seeat.server.global.response.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

public interface UserControllerSpec {

    /**
     * 회원가입 API
     * @param request 추가 정보 요청값
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

    /**
     * 사용자 정보 조회 API
     *
     * @param user 유저
     * @return UserInfoResponse 응답
     */
    @GetMapping
    @Operation(summary = "사용자 정보 조회",
                description = "마이페이지에서 사용자 정보를 조회합니다.")
    ApiResponse<UserInfoResponse> getUserInfo(
            @AuthenticationPrincipal User user);

    /**
     * 사용자 정보 수정 API
     *
     * @param user 유저
     * @param request 수정할 정보
     * @return UserInfoUpdateResponse 응답
     */
    @PatchMapping
    @Operation(summary = "사용자 정보 수정",
            description = "마이페이지에서 사용자 정보를 수정합니다.")
    ApiResponse<UserInfoUpdateResponse> updateUserInfo(
            @AuthenticationPrincipal User user,
            @RequestBody UserInfoUpdateRequest request);

    /**
     * 사용자 등급 목록 조회 API
     *
     * @return List UserGradeResponse 응답
     */
    @GetMapping("/grades")
    @Operation(summary = "사용자 등급 목록 조회",
            description = "사용자 등급 목록을 조회합니다.")
    ApiResponse<List<UserGradeResponse>> getUserGradeList();

}
