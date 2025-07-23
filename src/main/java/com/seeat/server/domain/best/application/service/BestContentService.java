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
import com.seeat.server.domain.theater.application.TheaterUseCase;
import com.seeat.server.domain.best.application.dto.response.BestAuditoriumListResponse;
import com.seeat.server.global.response.pageable.PageRequest;
import com.seeat.server.global.response.pageable.PageUtil;
import com.seeat.server.global.response.pageable.SliceResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.data.domain.SliceImpl;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.util.List;
import java.util.Optional;

import static com.seeat.server.global.util.RedisKeyUtil.*;

/**
 * [기존 서비스들을 활용하여 인기가 있는 리뷰/상영관을 조회하는 서비스]
 * - 6시간마다 스케쥴링으로 작동
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

    public SliceResponse<BestReviewListResponse> loadBestReviews(PageRequest pageRequest) {

        /// 페이징
        Pageable pageable = PageUtil.getPageable(pageRequest);

        /// 레디스에서 있는지 가져오기
        Optional<String> cachedJson = Optional.ofNullable(redisTemplate.opsForValue().get(BEST_REVIEW_LIST_KEY));

        /// 레디스에 존재한다면
        if (cachedJson.isPresent()) {
            try {
                List<BestReviewSnapshot> snapshots = objectMapper.readValue(
                        cachedJson.get(),
                        objectMapper.getTypeFactory().constructCollectionType(List.class, BestReviewSnapshot.class)
                );

                /// DTO 변경
                log.info("[BestContent] 레디스에서 조회");
                List<BestReviewListResponse> responses = BestReviewListResponse.from(snapshots);
                boolean hasNext = responses.size() == pageable.getPageSize();
                Slice<BestReviewListResponse> slice = new SliceImpl<>(responses, pageable, hasNext);

                return SliceResponse.from(slice);

            } catch (JsonProcessingException e) {
                /// 역직렬화 실패시 아래 DB 조회로 넘어가기에 로그만 찍어둔다.
                log.error("[BestContent] Redis 역직렬화 실패: {}", e.getMessage(), e);
            }
        }

        /// 레디스에 없다면 DB 조회를 통해서 제공
        log.info("[BestContent] DB에서 조회");
        return reviewService.getBestReviews(pageRequest);

    }

    /**
     * 베스트 상영관 목록 조회
     *
     * @param pageRequest 페이지
     */
    @Override
    public SliceResponse<BestAuditoriumListResponse> loadBestTheaters(PageRequest pageRequest) {

        /// 페이징
        Pageable pageable = PageUtil.getPageable(pageRequest);

        /// 레디스에서 있는지 가져오기
        Optional<String> cachedJson = Optional.ofNullable(redisTemplate.opsForValue().get(BEST_AUDITORIUM_LIST_KEY));

        /// 레디스에 존재한다면
        if (cachedJson.isPresent()) {
            try {
                List<BestAuditoriumSnapshot> snapshots = objectMapper.readValue(
                        cachedJson.get(),
                        objectMapper.getTypeFactory().constructCollectionType(List.class, BestAuditoriumSnapshot.class)
                );

                /// DTO 변경
                log.info("[BestContent] 레디스에서 조회");
                List<BestAuditoriumListResponse> responses = BestAuditoriumListResponse.from(snapshots);
                boolean hasNext = responses.size() == pageable.getPageSize();
                Slice<BestAuditoriumListResponse> slice = new SliceImpl<>(responses, pageable, hasNext);

                return SliceResponse.from(slice);

            } catch (JsonProcessingException e) {
                /// 역직렬화 실패시 아래 DB 조회로 넘어가기에 로그만 찍어둔다.
                log.error("[BestContent] Redis 역직렬화 실패: {}", e.getMessage(), e);
            }
        }

        /// 레디스에 없다면 DB 조회를 통해서 제공
        log.info("[BestContent] DB에서 조회");
        return theaterService.loadBestAuditoriums(pageRequest);
    }



    /**
     * TODO 스프링 배치를 통한 레디스 스케쥴링
     * TODO 레디스에 넣어두고, 스냅샷을 통해 TTL 이 없을 경우 대처하기 위해 DB에서 추가 구현
     * TODO! 6시간 간격마다 스케쥴링을 돌리거나 직접 버튼을 눌러 코드로 구현
     */
    @Override
    @Scheduled(cron = "0 0 * * * *")
    public void saveBestContents() {

        /// 페이지 요청
        PageRequest pageRequest = PageRequest.builder()
                .page(1)
                .size(20)
                .build();

        /// 리뷰 저장
        saveBestReviews(pageRequest);

        /// 상영관 저장
        saveBestAuditoriums(pageRequest);

    }


    /**
     * 인기 리뷰에서 삭제가 발생했을 때, 전체적으로 리뷰를 최신화하는 작업이 필요
     */
    @Override
    public void deleteBestContents() {


    }

    /**
     * 후기 저장을 위한 함수
     * @param pageRequest   페이징
     */
    private void saveBestReviews(PageRequest pageRequest) {

        /// 기존 값 삭제 후 새로 저장
        reviewRepository.deleteAll();

        /// 베스트 후기 조회 후, 저장하기
        SliceResponse<BestReviewListResponse> response = reviewService.getBestReviews(pageRequest);

        /// 베스트 후기 DB에 저장하기
        List<BestReviewSnapshot> reviewSnapshot = SnapShotConverter.toReviewSnapshot(response.content());

        /// 값이 없다면 저장하지 않는다.
        if (reviewSnapshot == null || reviewSnapshot.isEmpty()) {
            log.info("[BestContent] 스냅샷 변환 결과 없음 - Redis 저장 생략");
            return;
        }

        /// DB 저장
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
     * 상영관 저장을 위한 함수
     * @param pageRequest   페이징
     */
    private void saveBestAuditoriums(PageRequest pageRequest) {

        /// 기존 값 삭제 후 새로 저장
        auditoriumRepository.deleteAll();

        /// 인기 상영관 조회
        SliceResponse<BestAuditoriumListResponse> response = theaterService.loadBestAuditoriums(pageRequest);

        /// 스냅샷 변환
        List<BestAuditoriumSnapshot> auditoriumSnapshot = SnapShotConverter.toAuditoriumSnapshot(response.content());

        if (auditoriumSnapshot == null || auditoriumSnapshot.isEmpty()) {
            log.info("[BestContent] 스냅샷 변환 결과 없음 - Redis 저장 생략");
            return;
        }

        /// DB 저장
        auditoriumRepository.saveAll(auditoriumSnapshot);

        /// Redis 저장
        try {
            String auditoriumJson = objectMapper.writeValueAsString(auditoriumSnapshot);
            redisTemplate.opsForValue().set(BEST_AUDITORIUM_LIST_KEY, auditoriumJson, REDIS_TTL);
        } catch (JsonProcessingException e) {
            log.error("[BestContent] Redis 직렬화 실패: {}", e.getMessage(), e);
        }
    }


}
