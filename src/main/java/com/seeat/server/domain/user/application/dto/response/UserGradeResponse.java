package com.seeat.server.domain.user.application.dto.response;

import com.seeat.server.domain.user.domain.entity.UserGrade;
import io.swagger.v3.oas.annotations.media.Schema;

/**
 * 등급 목록 응답입니다.
 *
 * @param grade
 */
@Schema(name = "[응답][유저] 유저 등급 목록 Response",description = "유저 등급 목록 조회에 대한 DTO 입니다.")
public record UserGradeResponse(
        @Schema(description = "유저 등급", example = "BRONZE")
        UserGrade grade
) {

}
