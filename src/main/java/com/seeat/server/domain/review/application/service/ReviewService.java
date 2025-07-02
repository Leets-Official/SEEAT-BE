package com.seeat.server.domain.review.application.service;

import com.seeat.server.domain.review.application.usecase.ReviewUseCase;
import com.seeat.server.domain.review.domain.entity.Review;
import com.seeat.server.domain.review.domain.entity.ReviewHashTag;
import com.seeat.server.domain.review.domain.repository.ReviewHashTagRepository;
import com.seeat.server.domain.review.domain.repository.ReviewRepository;
import com.seeat.server.domain.review.presentation.dto.request.ReviewRequest;
import com.seeat.server.domain.review.presentation.dto.request.ReviewUpdateRequest;
import com.seeat.server.domain.review.presentation.dto.response.ReviewDetailResponse;
import com.seeat.server.domain.review.presentation.dto.response.ReviewListResponse;
import com.seeat.server.domain.theater.domain.entity.Seat;
import com.seeat.server.domain.theater.domain.repository.SeatRepository;
import com.seeat.server.domain.user.domain.entity.User;
import com.seeat.server.domain.user.domain.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.NoSuchElementException;

@Service
@Transactional
@RequiredArgsConstructor
public class ReviewService implements ReviewUseCase {

    private final ReviewRepository repository;
    private final ReviewHashTagRepository hashTagRepository;

    /// 외부 의존성 처리
    private final SeatRepository seatRepository;
    private final UserRepository userRepository;

    /**
     * 리뷰 저장을 위한 로직
     * @param request 리뷰를 위한 DTO
     * @param userId  리뷰를 작성할 유저 id (@AuthenticationPrincipal)
     */
    @Override
    public void createReview(ReviewRequest request, Long userId) {

        // 좌석 예외 처리
        Seat seat = seatRepository.findById(request.seatId())
                .orElseThrow(() -> new NoSuchElementException("해당 ID를 가진 좌석이 존재하지 않습니다."));

        // 유저 예외처리
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new NoSuchElementException("해당 ID를 가진 유저가 존재하지 않습니다."));

        // 객체 생성
        Review review = Review.of(user, seat, request.movieTitle(), request.rating(), request.content());

        // DB 내 저장
        repository.save(review);
    }

    /**
     * 리뷰 상세 조회를 위한 로직
     * @param reviewId 상세조회할 Id
     * @return 리뷰에 대한 상세 조회 DTO
     */
    @Override
    public ReviewDetailResponse loadReview(Long reviewId) {

        // ReviewId를 바탕으로 조회
        Review review = repository.findById(reviewId)
                .orElseThrow(() -> new NoSuchElementException("해당하는 리뷰가 존재하지 않습니다."));

        // ReviewId를 바탕으로 작성한 해시태그 조회
        List<ReviewHashTag> hashTags = hashTagRepository.findByReview_Id(reviewId);

        return ReviewDetailResponse.from(review, hashTags);
    }

    /**
     * 좌석에 따른 리뷰 목록 조회를 위한 로직
     * @param seatId 좌석 Id
     * @return 리뷰에 대한 목록 조회 DTO
     */
    @Override
    public List<ReviewListResponse> loadReviewsBySeatId(Long seatId) {

        // DB 조회
        List<Review> reviews = repository.findBySeat_Id(seatId);

        return List.of();
    }

    /**
     * 리뷰 수정을 위한 로직
     * @param request 수정을 위한 DTO
     * @param userId 수정을 원하는 유저 Id (@AuthenticationPrincipal)
     */
    @Override
    public void updateReview(ReviewUpdateRequest request, Long userId) {

    }

    /**
     * 리뷰 삭제를 위한 로직
     * @param reviewId 삭제를 위하는 리뷰 id
     * @param userId 삭제를 원하는 유저 Id (@AuthenticationPrincipal)
     */
    @Override
    public void deleteReview(Long reviewId, Long userId) {

    }

}

