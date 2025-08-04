package com.seeat.server.domain.manage.application.dto.response;

import com.seeat.server.domain.manage.domain.entity.Feedback;
import com.seeat.server.domain.user.application.dto.response.UserResponse;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.Builder;


/**
 * 피드백 상세조회 응답 DTO
 *
 * @param content       피드백 내용
 * @param user          작성자 정보 DTO
 */


@Builder
@Schema(name = "[응답][피드백] 피드백 상세 조회 Response",description = "피드백 상세 조회에 대한 DTO 입니다.")
public record FeedbackDetailResponse(

        @Schema(description = "피드백 내용", example = "메가박스 강남에 대한 정보가 틀린것 같아요!")
        String content,
        UserResponse user
) {

    public static FeedbackDetailResponse from(
            Feedback feedback

    ) {
        return FeedbackDetailResponse.builder()
                .content(feedback.getContent())
                .user(UserResponse
                        .from(feedback.getUser()))
                .build();
    }
}
