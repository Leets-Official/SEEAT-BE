package com.seeat.server.domain.manage.presentation;

import com.seeat.server.domain.manage.presentation.swagger.FeedbackControllerSpec;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/feedback")
@RequiredArgsConstructor
public class FeedbackController implements FeedbackControllerSpec {
}
