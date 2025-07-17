package com.seeat.server.domain.user.application.dto.response;

import com.seeat.server.domain.user.domain.entity.UserGrade;
import lombok.Builder;

public record UserGradeResponse(
        UserGrade grade
) {
}
