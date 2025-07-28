package com.seeat.server.domain.manage.application.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
@Schema(name = "[요청] 피드백 Request", description = "피드백 작성 시 사용되는 요청 객체입니다.")
public class FeedbackRequest {

    @Schema(description = "피드백 내용", example = "앱이 직관적이고 사용하기 편해요!")
    @NotNull(message = "피드백 내용은 필수입니다.")
    private String feedbackContent;
}
