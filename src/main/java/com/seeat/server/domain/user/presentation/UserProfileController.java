package com.seeat.server.domain.user.presentation;

import com.seeat.server.domain.user.application.dto.request.UserInfoUpdateRequest;
import com.seeat.server.domain.user.application.dto.response.UserGradeResponse;
import com.seeat.server.domain.user.application.dto.response.UserInfoResponse;
import com.seeat.server.domain.user.application.dto.response.UserInfoUpdateResponse;
import com.seeat.server.domain.user.application.usecase.UserProfileUseCase;
import com.seeat.server.domain.user.domain.entity.User;
import com.seeat.server.domain.user.presentation.swagger.UserProfileControllerSpec;
import com.seeat.server.global.response.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/profile")
public class UserProfileController implements UserProfileControllerSpec {

    private final UserProfileUseCase userProfileService;

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
        UserInfoResponse response = userProfileService.getUserInfo(user.getId());

        return ApiResponse.ok(response);
    }

    /**
     * 마이페이지에서 사용자 정보를 수정합니다.
     *
     * @param user Jwt 기반 SecurityContext 저장되어있는 유저
     * @param request 수정 정보 요청값 (닉네임, 유저프로필, 선호 장르, 선호 상영관)
     * @return UserInfoUpdateResponse DTO 응답
     */
    @PatchMapping
    public ApiResponse<UserInfoUpdateResponse> updateUserInfo(
            @AuthenticationPrincipal User user,
            @RequestBody UserInfoUpdateRequest request){

        // 사용자 정보 수정
        UserInfoUpdateResponse response = userProfileService.updateUserInfo(user.getId(), request);

        return ApiResponse.ok(response);
    }

    /**
     * 등급 목록을 조회합니다.
     *
     * @return UserGradeResponse DTO List 응답
     */
    @GetMapping("/grades")
    public ApiResponse<List<UserGradeResponse>> getUserGradeList(){

        // 등급 목록 조회
        List<UserGradeResponse> responses = userProfileService.getUserGradeList();

        return ApiResponse.ok(responses);
    }
}
