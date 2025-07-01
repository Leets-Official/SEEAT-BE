package com.seeat.server.domain.review.application.service;

import com.seeat.server.domain.review.application.usecase.ReviewUseCase;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;



@Service
@Transactional
@RequiredArgsConstructor
public class ReviewService implements ReviewUseCase {


}
