package com.seeat.server.domain.review.application.usecase;

/**
 * 좋아요 관련 기능 인터페이스입니다.
 */
public interface ReviewLikeUseCase {

    /// 좋아요 생성
    void reviewLike(Long userId, Long reviewId);

    /// 좋아요 삭제
    void reviewCancel(Long userId, Long reviewId);

}
