package com.seeat.server.domain.user.presentation.swagger;

import com.seeat.server.domain.review.application.dto.response.ReviewListResponse;
import com.seeat.server.domain.user.application.dto.request.UserInfoUpdateRequest;
import com.seeat.server.domain.user.application.dto.response.UserGradeResponse;
import com.seeat.server.domain.user.application.dto.response.UserInfoResponse;
import com.seeat.server.domain.user.application.dto.response.UserInfoUpdateResponse;
import com.seeat.server.domain.user.domain.entity.User;
import com.seeat.server.global.response.ApiResponse;
import com.seeat.server.global.response.pageable.PageRequest;
import com.seeat.server.global.response.pageable.SliceResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PatchMapping;

import java.io.IOException;
import java.util.List;

@Tag(name = "유저 프로필(마이페이지) API", description = "회원 정보를 관리하는 API 입니다.")
public interface UserProfileControllerSpec {
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
    @PatchMapping(
            consumes = MediaType.MULTIPART_FORM_DATA_VALUE
    )
    @Operation(summary = "사용자 정보 수정",
            description = "마이페이지에서 사용자 정보를 수정합니다.")
    ApiResponse<UserInfoUpdateResponse> updateUserInfo(
            @AuthenticationPrincipal User user,
            @ModelAttribute @Valid UserInfoUpdateRequest request) throws IOException;

    /**
     * 사용자 등급 목록 조회 API
     *
     * @return List UserGradeResponse 응답
     */
    @GetMapping("/grades")
    @Operation(summary = "사용자 등급 목록 조회",
            description = "사용자 등급 목록을 조회합니다.")
    ApiResponse<List<UserGradeResponse>> getUserGradeList();


    /**
     * 내가 작성한 리뷰 조회
     *
     * @param user        유저
     * @param pageRequest 페이지
     */
    @Operation(
            summary = "내가 작성한 리뷰 조회",
            description = "JWT 기반으로 리뷰 목록을 조회합니다."
    )
    @GetMapping("/reviews")
    ApiResponse<SliceResponse<ReviewListResponse>> getMyReviews(
            @AuthenticationPrincipal User user,
            PageRequest pageRequest);

    /**
     * 내가 북마크한 리뷰 조회
     * @param user          유저
     * @param pageRequest   페이지
     */
    @GetMapping("/bookmark")
    @Operation(
            description = "북마크 조회 API",
            summary = "북마크 조회 API 입니다."
    )
    ApiResponse<SliceResponse<ReviewListResponse>> getBookmarksByUser(
            @AuthenticationPrincipal User user,
            PageRequest pageRequest);


    /**
     * 사용자 탈퇴 상태로 수정
     *
     * @param user 유저
     */
    @DeleteMapping
    @Operation(
            description = "사용자 탈퇴 API",
            summary = "사용자 탈퇴 API 입니다."
    )
    ApiResponse<Void> deactivateUser(
            @Parameter(hidden = true)
            @AuthenticationPrincipal User user);
}
