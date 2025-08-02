package com.seeat.server.domain.user.application.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;

@Builder
@Schema(name = "[응답][유저] 유저 닉네임 중복 확인 Response",description = "유저 닉네임 중복 확인에 대한 DTO 입니다.")
public record UserNicknameResponse(
        @Schema(description = "중복 여부", example = "false")
        boolean duplicated,
        @Schema(description = "결과 메시지", example = "사용 가능한 닉네임입니다.")
        String message
) {

    // 정적 팩토리 메소드
    public static UserNicknameResponse from(boolean duplicated){
        return UserNicknameResponse.builder()
                .duplicated(duplicated)
                .message(duplicated
                        ? "닉네임이 사용 중입니다. 다른 닉네임을 사용해주세요."
                        : "닉네임 사용 가능합니다.")
                .build();
    }
}
