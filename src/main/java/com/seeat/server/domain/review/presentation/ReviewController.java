package com.seeat.server.domain.review.presentation;

import com.seeat.server.domain.review.presentation.swagger.ReviewControllerSpec;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/reviews")
public class ReviewController implements ReviewControllerSpec {
}
