package com.seeat.server.domain.review.application.service;

import com.seeat.server.domain.review.application.usecase.ReviewLikeUseCase;
import com.seeat.server.domain.review.application.usecase.ReviewUseCase;
import com.seeat.server.domain.review.domain.entity.Review;
import com.seeat.server.domain.review.domain.entity.ReviewLike;
import com.seeat.server.domain.review.domain.repository.ReviewLikeRepository;
import com.seeat.server.domain.user.application.UserUseCase;
import com.seeat.server.domain.user.domain.entity.User;
import com.seeat.server.global.response.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
@RequiredArgsConstructor
public class ReviewLikeService implements ReviewLikeUseCase {

    private final ReviewLikeRepository repository;

    /// 외부 의존성 주입
    private final UserUseCase userService;
    private final ReviewUseCase reviewService;

    /**
     * 리뷰 좋아요를 누르는 함수
     * @param userId        좋아요를 누르는 유저
     * @param reviewId      좋아요를 남길 리뷰
     */
    @Override
    public void reviewLike(Long userId, Long reviewId) {

        /// 유저 예외 처리
        User user = userService.getUser(userId);

        /// 리뷰 예외 처리
        Review review = reviewService.getReview(reviewId);

        /// 중복 여부 판단
        boolean checked = repository.existsByUserAndReview(user, review);
        if (checked) {
            throw new IllegalStateException(ErrorCode.DUPLICATE_REVIEW.getMessage());
        }

        /// 객체 생성
        ReviewLike reviewLike = ReviewLike.of(user, review);

        repository.save(reviewLike);
    }

    /**
     * 리뷰 좋아요를 해제하는 함수
     * @param userId        해제할 유저
     * @param reviewId      해제할 리뷰 ID
     */
    @Override
    public void reviewCancel(Long userId, Long reviewId) {

        /// 유저 예외 처리
        User user = userService.getUser(userId);

        /// 리뷰 예외 처리
        Review review = reviewService.getReview(reviewId);

        /// 추가되어있는지 여부 판단
        boolean checked = repository.existsByUserAndReview(user, review);
        if (!checked) {
            throw new IllegalStateException(ErrorCode.NOT_OWN_REVIEW.getMessage());
        }

        /// 삭제
        repository.deleteByUserAndReview(user, review);
    }
}
