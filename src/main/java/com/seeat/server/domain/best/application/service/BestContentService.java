package com.seeat.server.domain.best.application.service;

import com.seeat.server.domain.best.application.usecase.BestContentUseCase;
import com.seeat.server.domain.best.domain.entity.BestReviewSnapshot;
import com.seeat.server.domain.best.domain.repository.BestAuditoriumRepository;
import com.seeat.server.domain.best.domain.repository.BestReviewRepository;
import com.seeat.server.domain.review.application.dto.response.ReviewHashTagResponse;
import com.seeat.server.domain.review.application.dto.response.ReviewListResponse;
import com.seeat.server.domain.review.application.usecase.ReviewUseCase;
import com.seeat.server.domain.theater.application.TheaterUseCase;
import com.seeat.server.domain.best.application.dto.response.BestAuditoriumListResponse;
import com.seeat.server.domain.user.application.dto.response.UserResponse;
import com.seeat.server.global.response.pageable.PageRequest;
import com.seeat.server.global.response.pageable.SliceResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;

/**
 * [기존 서비스들을 활용하여 인기가 있는 리뷰/상영관을 조회하는 서비스]
 * - 6시간마다 스케쥴링으로 작동
 * - 레디스 캐싱 기능 구현
 * - 인기 상영관 목록 조회
 * - 인기 리뷰 목록 조회
 */
@Service
@Transactional
@RequiredArgsConstructor
public class BestContentService implements BestContentUseCase {

    private final BestReviewRepository reviewRepository;
    private final BestAuditoriumRepository auditoriumRepository;
    private final RedisTemplate<String, String> redisTemplate;

    /// 외부 의존성
    private final ReviewUseCase reviewService;
    private final TheaterUseCase theaterService;

    /**
     * 베스트 후기 목록 조회
     * @param pageRequest   페이지
     */
    @Override

    public SliceResponse<ReviewListResponse> loadBestReviews(PageRequest pageRequest) {

        /// 좋아요가 많은 인기 리뷰
        return reviewService.getBestReviews(pageRequest);
    }

    /**
     * 베스트 상영관 목록 조회
     *
     * @param pageRequest 페이지
     */
    @Override
    public SliceResponse<BestAuditoriumListResponse> loadBestTheaters(PageRequest pageRequest) {

        /// 결과 출력하기
        return theaterService.loadBestAuditoriums(pageRequest);
    }

    /**
     * TODO 스프링 배치를 통한 레디스 스케쥴링
     * TODO 레디스에 넣어두고, 스냅샷을 통해 TTL 이 없을 경우 대처하기 위해 DB에서 추가 구현
     * TODO! 6시간 간격마다 스케쥴링을 돌리거나 직접 버튼을 눌러 코드로 구현
     */
    @Override
    @Scheduled(cron = "0 0 */6 * * *")
    public void saveBestReviews() {

        /// 페이지 요청
        PageRequest pageRequest = PageRequest.builder()
                .page(1)
                .size(20)
                .build();

        /// 베스트 후기 조회 후, 저장하기
        SliceResponse<ReviewListResponse> reviews = reviewService.getBestReviews(pageRequest);

        /// 베스트 후기 DB에 저장하기
        List<BestReviewSnapshot> snapshots = CreateSnapShot(reviews.content());
        reviewRepository.saveAll(snapshots);


        /// 레디스에 저장하기

        /// 베스트 영화관 조회 후, 저장하기
        SliceResponse<BestAuditoriumListResponse> response = theaterService.loadBestAuditoriums(pageRequest);

        /// 베스트 영화관 DB에 저장하기

        /// 레디스에 저장하기

    }

    /**
     * 내부 함수
     * @param response  기존 리뷰 목록 DTO 바탕으로 스냅샷 생성
     */
    private static BestReviewSnapshot CreateSnapShot(ReviewListResponse response) {

        /// 해시태그 정보들
        List<String> hashTags = response.hashtags().stream()
                .map(ReviewHashTagResponse::hashTagName)
                .toList();

        /// 유저 정보들
        UserResponse user = response.user();

        return BestReviewSnapshot.of(
                response.reviewId(),
                hashTags,
                response.thumbnailUrl(),
                response.movieTitle(),
                response.theaterName(),
                response.content(),
                user.userId(),
                user.nickname(),
                user.profileImageUrl(),
                response.heartCount()
        );
    }

    private static List<BestReviewSnapshot> CreateSnapShot(List<ReviewListResponse> responses) {
        return responses.stream()
                .map(BestContentService::CreateSnapShot)
                .toList();
    }




}
