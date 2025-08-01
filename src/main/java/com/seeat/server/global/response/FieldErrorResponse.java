package com.seeat.server.global.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;

/**
 * 파라미터 오류에 대한 예외처리 입니다.
 *
 * @param field   에러가 발생한 필드명
 * @param message 해당 필드의 에러 메시지
 */

@Builder
@Schema(name = "[응답][공통] 필드 에러 Response", description = "파라미터 검증 시 발생한 필드별 오류 정보를 담는 DTO입니다.")
public record FieldErrorResponse(
        @Schema(description = "에러가 발생한 필드명", example = "username")
        String field,

        @Schema(description = "해당 필드의 에러 메시지", example = "필수 입력 값입니다.")
        String message) {

    /// 정적 팩토리 메서드
    public static FieldErrorResponse of(String field, String message) {
        return FieldErrorResponse.builder()
                .field(field)
                .message(message)
                .build();
    }
}
