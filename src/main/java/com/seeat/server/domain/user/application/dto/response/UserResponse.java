package com.seeat.server.domain.user.application.dto.response;

import com.seeat.server.domain.user.domain.entity.User;
import lombok.Builder;

/**
 * 유저 정보 응답 DTO
 *
 * @param userId           유저 고유 ID
 * @param nickname         유저 닉네임
 * @param profileImageUrl  유저 프로필 이미지 URL
 */
@Builder
public record UserResponse(
        Long userId,
        String nickname,
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
