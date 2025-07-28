package com.seeat.server.domain.manage.application.dto.response;

import com.seeat.server.domain.manage.domain.entity.Feedback;
import com.seeat.server.domain.user.application.dto.response.UserResponse;
import lombok.Builder;


/**
 * 피드백 상세조회 응답 DTO
 *
 * @param content       피드백 내용
 * @param user          작성자 정보 DTO
 */


@Builder
public record FeedbackDetailResponse(
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
