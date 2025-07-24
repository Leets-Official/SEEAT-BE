package com.seeat.server.domain.best.application.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.seeat.server.domain.best.application.dto.response.BestReviewListResponse;
import com.seeat.server.domain.best.application.usecase.BestContentUseCase;
import com.seeat.server.domain.best.application.util.SnapShotConverter;
import com.seeat.server.domain.best.domain.entity.BestAuditoriumSnapshot;
import com.seeat.server.domain.best.domain.entity.BestReviewSnapshot;
import com.seeat.server.domain.best.domain.repository.BestAuditoriumRepository;
import com.seeat.server.domain.best.domain.repository.BestReviewRepository;
import com.seeat.server.domain.review.application.usecase.ReviewUseCase;
import com.seeat.server.domain.best.application.dto.response.BestAuditoriumListResponse;
import com.seeat.server.domain.theater.application.usecase.TheaterUseCase;
import com.seeat.server.global.response.pageable.PageRequest;
import com.seeat.server.global.response.pageable.PageUtil;
import com.seeat.server.global.response.pageable.SliceResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.data.domain.SliceImpl;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.util.List;

import static com.seeat.server.global.util.RedisKeyUtil.*;

/**
 * [기존 서비스들을 활용하여 인기가 있는 리뷰/상영관을 조회하는 서비스]
 * - 1시간마다 스케쥴링으로 작동
 * - 레디스 캐싱 기능 구현
 * - 인기 상영관 목록 조회
 * - 인기 리뷰 목록 조회
 */
@Slf4j
@Service
@Transactional
@RequiredArgsConstructor
public class BestContentService implements BestContentUseCase {

    private final BestReviewRepository reviewRepository;
    private final BestAuditoriumRepository auditoriumRepository;

    /// 레디스 저장 의존성
    private final RedisTemplate<String, String> redisTemplate;
    private final ObjectMapper objectMapper;
    private static final Duration REDIS_TTL = Duration.ofHours(1);

    /// 외부 의존성
    private final ReviewUseCase reviewService;
    private final TheaterUseCase theaterService;

    /**
     * 베스트 후기 목록 조회
     * @param pageRequest   페이지
     */
    @Override
    @Cacheable(value = BEST_REVIEW_LIST_KEY, key = "#pageRequest.page + '-' + #pageRequest.size")
    public SliceResponse<BestReviewListResponse> loadBestReviews(PageRequest pageRequest) {
        log.info("[BestContent] DB에서 조회 (캐시 없거나 만료)");

        /// 스냅샷 테이블에서 페이징 조회
        Pageable pageable = PageUtil.getPageable(pageRequest);
        Slice<BestReviewSnapshot> snapshots = reviewRepository.findAll(pageable);

        /// 기존 서비스에서 조회
        if (snapshots.isEmpty()) {
            log.info("[BestContent] 기존 쿼리에서 조회");
            return reviewService.getBestReviews(pageRequest);
        }

        List<BestReviewListResponse> responses = BestReviewListResponse.from(snapshots.getContent());
        Slice<BestReviewListResponse> slice = new SliceImpl<>(responses, snapshots.getPageable(), snapshots.hasNext());
        return SliceResponse.from(slice);

    }

    /**
     * 베스트 상영관 목록 조회
     * @param pageRequest 페이지
     */
    @Override
    @Cacheable(value = BEST_AUDITORIUM_LIST_KEY, key = "#pageRequest.page + '-' + #pageRequest.size")
    public SliceResponse<BestAuditoriumListResponse> loadBestTheaters(PageRequest pageRequest) {

        log.info("[BestContent] DB에서 조회 (캐시 없거나 만료)");

        /// 스냅샷 테이블에서 페이징 조회
        Pageable pageable = PageUtil.getPageable(pageRequest);
        Slice<BestAuditoriumSnapshot> snapshots = auditoriumRepository.findAll(pageable);

        /// 기존 서비스에서 조회
        if (snapshots.isEmpty()) {
            log.info("[BestContent] 기존 쿼리에서 조회");
            return theaterService.loadBestAuditoriums(pageRequest);
        }

        List<BestAuditoriumListResponse> responses = BestAuditoriumListResponse.from(snapshots.getContent());
        Slice<BestAuditoriumListResponse> slice = new SliceImpl<>(responses, snapshots.getPageable(), snapshots.hasNext());
        return SliceResponse.from(slice);
    }

    /**
     * 6시간마다 캐시 및 DB 최신화 작업 수행
     */
    @Override
    @Scheduled(cron = "0 0 * * * *") // 1시간 간격
    public void saveBestContents() {
        PageRequest pageRequest = PageRequest.builder()
                .page(1)
                .size(20)
                .build();

        // 캐시 업데이트를 위해 별도 메서드 호출
        saveBestReviews(pageRequest);
        saveBestAuditoriums(pageRequest);
    }

    /**
     * 인기 리뷰 캐시, DB 최신화
     */
    @CacheEvict(value = BEST_REVIEW_LIST_KEY, allEntries = true)
    public void saveBestReviews(PageRequest pageRequest) {

        /// 기존 스냅샷 삭제
        reviewRepository.deleteAll();

        SliceResponse<BestReviewListResponse> response = reviewService.getBestReviews(pageRequest);
        List<BestReviewSnapshot> reviewSnapshot = SnapShotConverter.toReviewSnapshot(response.content());

        /// 결과없기에 패스
        if (reviewSnapshot == null || reviewSnapshot.isEmpty()) {
            log.info("[BestContent] 스냅샷 변환 결과 없음 - 캐시 저장 생략");
            return;
        }

        /// 스냅샷 DB 저장
        reviewRepository.saveAll(reviewSnapshot);

        /// 레디스에 저장하기
        try {
            String reviewJson = objectMapper.writeValueAsString(reviewSnapshot);
            redisTemplate.opsForValue()
                    .set(BEST_REVIEW_LIST_KEY, reviewJson, REDIS_TTL);
        } catch (JsonProcessingException e) {
            log.error("[BestContent] Redis 직렬화 실패: {}", e.getMessage(), e);
        }
    }

    /**
     * 인기 상영관 캐시, DB 최신화
     */
    @CacheEvict(value = BEST_AUDITORIUM_LIST_KEY, allEntries = true)
    public void saveBestAuditoriums(PageRequest pageRequest) {

        /// 기존 스냅샷 삭제
        auditoriumRepository.deleteAll();

        SliceResponse<BestAuditoriumListResponse> response = theaterService.loadBestAuditoriums(pageRequest);
        List<BestAuditoriumSnapshot> auditoriumSnapshot = SnapShotConverter.toAuditoriumSnapshot(response.content());

        if (auditoriumSnapshot == null || auditoriumSnapshot.isEmpty()) {
            log.info("[BestContent] 스냅샷 변환 결과 없음 - 캐시 저장 생략");
            return;
        }

        /// 스냅샷 DB 저장
        auditoriumRepository.saveAll(auditoriumSnapshot);

        /// Redis 저장
        try {
            String auditoriumJson = objectMapper.writeValueAsString(auditoriumSnapshot);
            redisTemplate.opsForValue().set(BEST_AUDITORIUM_LIST_KEY, auditoriumJson, REDIS_TTL);
        } catch (JsonProcessingException e) {
            log.error("[BestContent] Redis 직렬화 실패: {}", e.getMessage(), e);
        }
    }

    /**
     * 인기 데이터 전체 삭제
     */
    @Override
    @CacheEvict(value = {BEST_REVIEW_LIST_KEY, BEST_AUDITORIUM_LIST_KEY}, allEntries = true)
    public void deleteBestContents() {
        reviewRepository.deleteAll();
        auditoriumRepository.deleteAll();
    }
}
