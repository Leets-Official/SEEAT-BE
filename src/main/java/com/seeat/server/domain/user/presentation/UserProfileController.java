package com.seeat.server.domain.user.presentation;

import com.seeat.server.domain.review.application.dto.response.ReviewListResponse;
import com.seeat.server.domain.bookmark.application.usecase.BookmarkUseCase;
import com.seeat.server.domain.review.application.usecase.ReviewUseCase;
import com.seeat.server.domain.user.application.dto.request.UserInfoUpdateRequest;
import com.seeat.server.domain.user.application.dto.response.UserGradeResponse;
import com.seeat.server.domain.user.application.dto.response.UserInfoResponse;
import com.seeat.server.domain.user.application.dto.response.UserInfoUpdateResponse;
import com.seeat.server.domain.user.application.usecase.UserProfileUseCase;
import com.seeat.server.domain.user.domain.entity.User;
import com.seeat.server.domain.user.presentation.swagger.UserProfileControllerSpec;
import com.seeat.server.global.response.ApiResponse;
import com.seeat.server.security.oauth2.application.dto.response.CustomUserInfo;
import io.swagger.v3.oas.annotations.Parameter;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import com.seeat.server.global.response.pageable.PageRequest;
import com.seeat.server.global.response.pageable.SliceResponse;
import org.springframework.data.domain.Slice;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/profile")
public class UserProfileController implements UserProfileControllerSpec {

    private final UserProfileUseCase userProfileService;

    /// 북마크 및 리뷰 조회
    private final BookmarkUseCase bookmarkService;
    private final ReviewUseCase reviewService;

    /**
     * 마이페이지에서 사용자 정보를 조회 합니다.
     *
     * @param user Jwt 기반 SecurityContext 저장되어있는 유저
     * @return UserInfoResponse DTO 응답
     */
    @GetMapping
    public ApiResponse<UserInfoResponse> getUserInfo(
            @AuthenticationPrincipal CustomUserInfo user){

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
    @PatchMapping()
    public ApiResponse<UserInfoUpdateResponse> updateUserInfo(
            @AuthenticationPrincipal CustomUserInfo user,
            @RequestBody @Valid UserInfoUpdateRequest request) throws IOException {

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

    /**
     * 내가 작성한 리뷰 조회
     *
     * @param user        유저
     * @param pageRequest 페이지
     */
    @GetMapping("/reviews")
    public ApiResponse<SliceResponse<ReviewListResponse>> getMyReviews(
            @AuthenticationPrincipal CustomUserInfo user,
            PageRequest pageRequest) {

        /// 서비스 호출
        SliceResponse<ReviewListResponse> response = reviewService.loadMyReviews(user.getId(), pageRequest);

        return ApiResponse.ok(response);
    }

    /**
     * 나의 북마크를 조회
     *
     * @param user        유저
     * @param pageRequest 페이지
     */
    @GetMapping("/bookmark")
    public ApiResponse<SliceResponse<ReviewListResponse>> getBookmarksByUser(
            @AuthenticationPrincipal CustomUserInfo user,
            PageRequest pageRequest) {

        /// 서비스 호출
        Slice<ReviewListResponse> responses = bookmarkService.loadMyBookmarks(user.getId(), pageRequest);

        /// DTO 변환
        SliceResponse<ReviewListResponse> response = SliceResponse.from(responses);

        return ApiResponse.ok(response);
    }

    /**
     * 사용자 탈퇴 상태로 수정
     *
     * @param user 유저
     */
    @DeleteMapping
    public ApiResponse<Void> deactivateUser(
            @Parameter(hidden = true)
            @AuthenticationPrincipal CustomUserInfo user){

        // 삭제 서비스
        userProfileService.deactivateUser(user.getId());

        // 리턴
        return ApiResponse.deleted();
    }

}
