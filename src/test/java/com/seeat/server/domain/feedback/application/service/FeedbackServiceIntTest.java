package com.seeat.server.domain.feedback.application.service;

import com.seeat.server.domain.manage.application.FeedbackService;
import com.seeat.server.domain.manage.application.dto.request.FeedbackRequest;
import com.seeat.server.domain.manage.domain.entity.Feedback;
import com.seeat.server.domain.manage.domain.repository.FeedbackRepository;
import com.seeat.server.domain.user.domain.entity.User;
import com.seeat.server.domain.user.domain.repository.UserRepository;
import com.seeat.server.global.response.ErrorCode;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.NoSuchElementException;

import static com.seeat.server.domain.user.domain.UserFixtures.createUser;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;


@SpringBootTest
@ActiveProfiles("test")
@Transactional
public class FeedbackServiceIntTest {

    @Autowired
    private FeedbackService feedbackService;

    @Autowired
    private FeedbackRepository feedbackRepository;

    @Autowired
    private UserRepository userRepository;

    @Test
    @DisplayName("유저가 피드백을 정상적으로 등록")
    void createFeedback_Success() {
        // given
        User user = userRepository.save(createUser());

        FeedbackRequest request = FeedbackRequest.builder()
                .feedbackContent("테스트 피드백")
                .build();

        // when
        feedbackService.createFeedback(request, user.getId());

        // then
        List<Feedback> result = feedbackRepository.findAll();

        // 1. 개수 검증
        assertThat(result).hasSize(1);

        // 2. 내용 검증
        Feedback saved = result.get(0);
        assertThat(saved.getUser().getId()).isEqualTo(user.getId());
        assertThat(saved.getContent()).isEqualTo("테스트 피드백");
    }

    @Test
    @DisplayName("존재하지 않는 유저가 피드백 등록 시 예외가 발생")
    void createFeedback_Fail_UserNotFound() {
        // given
        Long notExistUserId = 99999L;

        FeedbackRequest request = FeedbackRequest.builder()
                .feedbackContent("예외 발생 테스트")
                .build();

        // when & then
        assertThatThrownBy(() ->
                feedbackService.createFeedback(request, notExistUserId)
        )
                .isInstanceOf(NoSuchElementException.class)
                .hasMessageContaining(ErrorCode.NOT_USER.getMessage());
    }

    @Test
    @DisplayName("피드백 내용이 null이면 예외가 발생")
    void createFeedback_Fail_NullContent() {
        // given
        User user = userRepository.save(createUser());

        FeedbackRequest request = FeedbackRequest.builder()
                .feedbackContent(null)
                .build();

        // when & then
        assertThatThrownBy(() ->
                feedbackService.createFeedback(request, user.getId())
        )
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining(ErrorCode.INVALID_FEEDBACK_CONTENT.getMessage());
    }

}
