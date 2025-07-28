package com.seeat.server.domain.manage.application.dto.request;

import jakarta.validation.constraints.NotNull;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class FeedbackRequest {

    /**
     * 피드백 ID
     */
    @NotNull(message = "피드백 ID는 필수입니다.")
    private Long feedbackId;

    /**
     * 유저 ID
     */
    @NotNull(message = "유저 아이디는 필수입니다.")
    private Long userId;

    /**
     * 피드백 내용
     */
    @NotNull(message = "피드백 내용은 필수입니다.")
    private String feedbackContent;

}
