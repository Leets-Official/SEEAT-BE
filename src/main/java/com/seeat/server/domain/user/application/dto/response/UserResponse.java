package com.seeat.server.domain.user.application.dto.response;

import com.seeat.server.domain.user.domain.entity.User;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;

/**
 * 유저 정보 응답 DTO
 *
 * @param userId           유저 고유 ID
 * @param nickname         유저 닉네임
 * @param profileImageUrl  유저 프로필 이미지 URL
 */
@Builder
@Schema(name = "[응답][리뷰] 유저 정보 Response",description = "리뷰 상세 조회에 포함되는 유저 정보에 대한 DTO 입니다.")
public record UserResponse(
        @Schema(description = "유저 고유 ID", example = "12345")
        Long userId,

        @Schema(description = "유저 닉네임", example = "happyUser")
        String nickname,

        @Schema(description = "유저 프로필 이미지 URL", example = "https://example.com/profiles/happyUser.jpg")
        String profileImageUrl
) {
    // 정적 팩토리 메서드
    public static UserResponse from(User user) {
        return UserResponse.builder()
                .userId(user.getId())
                .nickname(user.getNickname())
                .profileImageUrl(user.getImageUrl())
                .build();
    }
}
