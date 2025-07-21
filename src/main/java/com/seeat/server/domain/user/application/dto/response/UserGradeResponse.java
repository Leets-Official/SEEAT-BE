package com.seeat.server.domain.user.application.dto.response;

import com.seeat.server.domain.user.domain.entity.UserGrade;

/**
 * 등급 목록 응답입니다.
 *
 * @param grade
 */
public record UserGradeResponse(
        UserGrade grade
) {

}
